package ir.appservice.infrastructure.i18n.event;

import ir.appservice.infrastructure.i18n.message.DynamicMessageSource;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * High-level coordinator for L1 cache eviction and cross-cluster Redis event broadcasting.
 */
@Component
public class I18nCacheInvalidator {

    private final DynamicMessageSource messageSource;
    private final I18nEventPublisher eventPublisher;
    private final String nodeId;

    public I18nCacheInvalidator(DynamicMessageSource messageSource, I18nEventPublisher eventPublisher) {
        this.messageSource = messageSource;
        this.eventPublisher = eventPublisher;
        this.nodeId = UUID.randomUUID().toString();
    }

    public void evict(String tenantId, String locale, String messageKey) {
        // 1. Evict local L1 Caffeine cache
        if (messageSource != null) {
            messageSource.invalidate(tenantId, locale, messageKey);
        }

        // 2. Broadcast to cluster nodes via Redis
        if (eventPublisher != null) {
            I18nInvalidationEvent event = I18nInvalidationEvent.ofKey(tenantId, locale, messageKey, nodeId);
            eventPublisher.publishInvalidation(event);
        }
    }

    public void evictAll() {
        if (messageSource != null) {
            messageSource.invalidateAll();
        }
        if (eventPublisher != null) {
            I18nInvalidationEvent event = I18nInvalidationEvent.ofAll(nodeId);
            eventPublisher.publishInvalidation(event);
        }
    }

    public String getNodeId() {
        return nodeId;
    }
}
