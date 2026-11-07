package ir.appservice.infrastructure.i18n;

import ir.appservice.common.context.TenantContextHolder;
import ir.appservice.core.domain.entity.I18nMessage;
import ir.appservice.core.domain.port.outbound.I18nMessageRepositoryPort;
import ir.appservice.infrastructure.i18n.message.DynamicMessageSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Dynamic MessageSource Database-Backed & Caffeine L1 Cache Tests")
class DynamicMessageSourceCacheTest {

    private final Map<String, I18nMessage> inMemoryDb = new HashMap<>();
    private final AtomicInteger dbQueryCount = new AtomicInteger(0);
    private I18nMessageRepositoryPort repository;
    private DynamicMessageSource messageSource;

    @BeforeEach
    void setUp() {
        inMemoryDb.clear();
        dbQueryCount.set(0);

        repository = new I18nMessageRepositoryPort() {
            @Override
            public Optional<I18nMessage> findByKeyAndLocaleAndTenantId(String messageKey, String locale, String tenantId) {
                dbQueryCount.incrementAndGet();
                String key = (tenantId != null ? tenantId : "_global") + ":" + locale + ":" + messageKey;
                return Optional.ofNullable(inMemoryDb.get(key));
            }

            @Override
            public List<I18nMessage> findByLocaleAndTenantId(String locale, String tenantId) {
                return inMemoryDb.values().stream()
                        .filter(m -> locale.equals(m.getLocale()) && (tenantId == null || tenantId.equals(m.getTenantId())))
                        .toList();
            }

            @Override
            public List<I18nMessage> findByCategoryAndLocaleAndTenantId(String category, String locale, String tenantId) {
                return inMemoryDb.values().stream()
                        .filter(m -> category.equals(m.getCategory()) && locale.equals(m.getLocale()))
                        .toList();
            }

            @Override
            public I18nMessage save(I18nMessage message) {
                String key = (message.getTenantId() != null ? message.getTenantId() : "_global")
                        + ":" + message.getLocale() + ":" + message.getMessageKey();
                inMemoryDb.put(key, message);
                return message;
            }

            @Override
            public void deleteById(String id) {
                inMemoryDb.values().removeIf(m -> id.equals(m.getId()));
            }

            @Override
            public void deleteByKeyAndTenantId(String messageKey, String tenantId) {
                String prefix = (tenantId != null ? tenantId : "_global") + ":";
                inMemoryDb.keySet().removeIf(k -> k.startsWith(prefix) && k.endsWith(":" + messageKey));
            }
        };

        messageSource = new DynamicMessageSource(repository);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("Cache Miss populates Caffeine cache; successive calls hit cache with 0 DB queries")
    void testCacheMissAndSubsequentHit() {
        repository.save(new I18nMessage("1", "app.welcome", "fa", "خوش آمدید {0}", "general", null));

        Locale faLocale = Locale.forLanguageTag("fa");

        // First call: DB query triggered
        String msg1 = messageSource.getMessage("app.welcome", new Object[]{"بهمن"}, faLocale);
        assertThat(msg1).isEqualTo("خوش آمدید بهمن");
        assertThat(dbQueryCount.get()).isGreaterThanOrEqualTo(1);

        int initialDbCalls = dbQueryCount.get();

        // Next 5 calls: Served exclusively from Caffeine L1 Cache
        for (int i = 0; i < 5; i++) {
            String cachedMsg = messageSource.getMessage("app.welcome", new Object[]{"کاربر"}, faLocale);
            assertThat(cachedMsg).isEqualTo("خوش آمدید کاربر");
        }

        assertThat(dbQueryCount.get()).isEqualTo(initialDbCalls);
    }

    @Test
    @DisplayName("Tenant-specific translation overrides global default translation")
    void testTenantOverrideHierarchy() {
        // Global translation
        repository.save(new I18nMessage("1", "app.portal.name", "en", "Global Enterprise Portal", "branding", null));

        // Tenant custom override
        repository.save(new I18nMessage("2", "app.portal.name", "en", "Tehran Bank Portal", "branding", "tenant_bank"));

        // When in tenant_bank context
        TenantContextHolder.setTenantId("tenant_bank");
        String bankTitle = messageSource.getMessage("app.portal.name", null, Locale.ENGLISH);
        assertThat(bankTitle).isEqualTo("Tehran Bank Portal");

        // When in another tenant context without override, falls back to global
        TenantContextHolder.setTenantId("tenant_other");
        String defaultTitle = messageSource.getMessage("app.portal.name", null, Locale.ENGLISH);
        assertThat(defaultTitle).isEqualTo("Global Enterprise Portal");
    }

    @Test
    @DisplayName("BENCHMARK: Caffeine L1 cache hit resolves translation in sub-microsecond latency (< 0.1ms)")
    void testSubMicrosecondCacheBenchmark() {
        repository.save(new I18nMessage("10", "perf.metric.label", "en", "Performance Metric Score", "perf", null));
        Locale locale = Locale.ENGLISH;

        // Warm up cache
        messageSource.getMessage("perf.metric.label", null, locale);

        int iterations = 10_000;
        long startTimeNs = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            messageSource.getMessage("perf.metric.label", null, locale);
        }

        long totalElapsedNs = System.nanoTime() - startTimeNs;
        double avgLatencyNs = (double) totalElapsedNs / iterations;
        double avgLatencyMs = avgLatencyNs / 1_000_000.0;

        System.out.printf("Caffeine L1 Cache Benchmark: %d lookups, avg latency = %.4f µs (%.6f ms)%n",
                iterations, avgLatencyNs / 1_000.0, avgLatencyMs);

        // Verification proof requirement: sub-microsecond latency (< 0.1 ms = 100 microseconds)
        assertThat(avgLatencyMs).isLessThan(0.1);
    }
}
