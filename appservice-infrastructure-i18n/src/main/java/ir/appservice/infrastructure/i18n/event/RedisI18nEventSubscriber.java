package ir.appservice.infrastructure.i18n.event;

import com.google.gson.Gson;
import ir.appservice.infrastructure.i18n.message.DynamicMessageSource;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.nio.charset.StandardCharsets;

/**
 * Redis Pub/Sub Event Subscriber.
 * Listens for cache invalidation events and evicts translations from local Caffeine L1 cache.
 */
public class RedisI18nEventSubscriber implements MessageListener {

    private final DynamicMessageSource messageSource;
    private final String localNodeId;
    private final Gson gson = new Gson();

    public RedisI18nEventSubscriber(DynamicMessageSource messageSource, String localNodeId) {
        this.messageSource = messageSource;
        this.localNodeId = localNodeId;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            I18nInvalidationEvent event = gson.fromJson(payload, I18nInvalidationEvent.class);
            if (event != null) {
                // Ignore self-published events if local cache was already evicted
                if (localNodeId != null && localNodeId.equals(event.publisherNodeId())) {
                    return;
                }
                processEvent(event);
            }
        } catch (Exception ignored) {
            // Log warning or silently ignore invalid payloads
        }
    }

    public void processEvent(I18nInvalidationEvent event) {
        if (event.invalidateAll()) {
            messageSource.invalidateAll();
        } else {
            messageSource.invalidate(event.tenantId(), event.locale(), event.messageKey());
        }
    }
}
