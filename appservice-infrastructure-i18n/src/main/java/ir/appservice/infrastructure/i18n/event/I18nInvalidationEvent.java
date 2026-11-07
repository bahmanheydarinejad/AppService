package ir.appservice.infrastructure.i18n.event;

import java.io.Serializable;

/**
 * Event published when dynamic localization messages are added, updated, or deleted.
 * Transmitted across cluster nodes via Redis Pub/Sub for live cache hot-reloading.
 */
public record I18nInvalidationEvent(
        String tenantId,
        String locale,
        String messageKey,
        boolean invalidateAll,
        String publisherNodeId
) implements Serializable {

    public static I18nInvalidationEvent ofKey(String tenantId, String locale, String messageKey, String nodeId) {
        return new I18nInvalidationEvent(tenantId, locale, messageKey, false, nodeId);
    }

    public static I18nInvalidationEvent ofAll(String nodeId) {
        return new I18nInvalidationEvent(null, null, null, true, nodeId);
    }
}
