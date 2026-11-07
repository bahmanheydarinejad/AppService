package ir.appservice.infrastructure.i18n.event;

/**
 * Event publisher for i18n cache eviction across nodes.
 */
public interface I18nEventPublisher {

    void publishInvalidation(I18nInvalidationEvent event);
}
