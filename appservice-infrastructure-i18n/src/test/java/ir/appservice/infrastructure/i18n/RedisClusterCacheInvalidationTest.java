package ir.appservice.infrastructure.i18n;

import ir.appservice.core.domain.entity.I18nMessage;
import ir.appservice.core.domain.port.outbound.I18nMessageRepositoryPort;
import ir.appservice.infrastructure.i18n.event.I18nCacheInvalidator;
import ir.appservice.infrastructure.i18n.event.I18nEventPublisher;
import ir.appservice.infrastructure.i18n.event.I18nInvalidationEvent;
import ir.appservice.infrastructure.i18n.event.RedisI18nEventSubscriber;
import ir.appservice.infrastructure.i18n.message.DynamicMessageSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Redis Pub/Sub Cluster Cache Invalidation Simulation Tests")
class RedisClusterCacheInvalidationTest {

    private final Map<String, I18nMessage> sharedDatabase = new HashMap<>();
    private I18nMessageRepositoryPort sharedRepository;

    private DynamicMessageSource nodeAMessageSource;
    private DynamicMessageSource nodeBMessageSource;

    private RedisI18nEventSubscriber nodeBSubscriber;
    private I18nCacheInvalidator nodeAInvalidator;

    private final List<I18nInvalidationEvent> publishedEvents = new ArrayList<>();

    @BeforeEach
    void setUp() {
        sharedDatabase.clear();
        publishedEvents.clear();

        sharedRepository = new I18nMessageRepositoryPort() {
            @Override
            public Optional<I18nMessage> findByKeyAndLocaleAndTenantId(String messageKey, String locale, String tenantId) {
                String key = (tenantId != null ? tenantId : "_global") + ":" + locale + ":" + messageKey;
                return Optional.ofNullable(sharedDatabase.get(key));
            }

            @Override
            public List<I18nMessage> findByLocaleAndTenantId(String locale, String tenantId) {
                return sharedDatabase.values().stream()
                        .filter(m -> locale.equals(m.getLocale()))
                        .toList();
            }

            @Override
            public List<I18nMessage> findByCategoryAndLocaleAndTenantId(String category, String locale, String tenantId) {
                return sharedDatabase.values().stream().toList();
            }

            @Override
            public I18nMessage save(I18nMessage message) {
                String key = (message.getTenantId() != null ? message.getTenantId() : "_global")
                        + ":" + message.getLocale() + ":" + message.getMessageKey();
                sharedDatabase.put(key, message);
                return message;
            }

            @Override public void deleteById(String id) {}
            @Override public void deleteByKeyAndTenantId(String messageKey, String tenantId) {}
        };

        // Node A & Node B separate message sources (representing separate JVM / container cluster nodes)
        nodeAMessageSource = new DynamicMessageSource(sharedRepository);
        nodeBMessageSource = new DynamicMessageSource(sharedRepository);

        // Node B subscriber
        nodeBSubscriber = new RedisI18nEventSubscriber(nodeBMessageSource, "node-b");

        // Simulated Redis Pub/Sub channel
        I18nEventPublisher redisSimulatedPublisher = event -> {
            publishedEvents.add(event);
            // Simulate delivery to Node B subscriber
            nodeBSubscriber.processEvent(event);
        };

        // Node A cache invalidator broadcasting to Redis
        nodeAInvalidator = new I18nCacheInvalidator(nodeAMessageSource, redisSimulatedPublisher);
    }

    @Test
    @DisplayName("Updating translation on Node A publishes Redis event and evicts Node B's local cache")
    void testCrossNodeCacheInvalidation() {
        // Initial state in shared DB
        I18nMessage original = new I18nMessage("1", "system.status", "en", "System Operational", "status", null);
        sharedRepository.save(original);

        Locale locale = Locale.ENGLISH;

        // Both Node A and Node B query and cache the initial message
        assertThat(nodeAMessageSource.getMessage("system.status", null, locale)).isEqualTo("System Operational");
        assertThat(nodeBMessageSource.getMessage("system.status", null, locale)).isEqualTo("System Operational");

        // Database is updated with new translation
        I18nMessage updated = new I18nMessage("1", "system.status", "en", "System Under Maintenance", "status", null);
        sharedRepository.save(updated);

        // Before invalidation, Node B would still return cached message
        // (confirming cache isolation)
        assertThat(nodeBMessageSource.getMessage("system.status", null, locale)).isEqualTo("System Operational");

        // Node A updates translation and triggers cluster cache invalidator
        nodeAInvalidator.evict(null, "en", "system.status");

        // Verify event was dispatched to Redis
        assertThat(publishedEvents).hasSize(1);
        assertThat(publishedEvents.get(0).messageKey()).isEqualTo("system.status");

        // Node B now immediately reflects updated translation from DB without JVM restart!
        String nodeBUpdatedMessage = nodeBMessageSource.getMessage("system.status", null, locale);
        assertThat(nodeBUpdatedMessage).isEqualTo("System Under Maintenance");
    }

    @Test
    @DisplayName("Evict All command purges all cached entries on peer cluster node")
    void testEvictAllAcrossCluster() {
        sharedRepository.save(new I18nMessage("1", "msg.1", "en", "Text 1", "general", null));
        sharedRepository.save(new I18nMessage("2", "msg.2", "en", "Text 2", "general", null));

        Locale locale = Locale.ENGLISH;
        nodeBMessageSource.getMessage("msg.1", null, locale);
        nodeBMessageSource.getMessage("msg.2", null, locale);
        assertThat(nodeBMessageSource.getCacheSize()).isGreaterThanOrEqualTo(2);

        // Evict all triggered from Node A
        nodeAInvalidator.evictAll();

        assertThat(publishedEvents).hasSize(1);
        assertThat(publishedEvents.get(0).invalidateAll()).isTrue();
        assertThat(nodeBMessageSource.getCacheSize()).isEqualTo(0);
    }
}
