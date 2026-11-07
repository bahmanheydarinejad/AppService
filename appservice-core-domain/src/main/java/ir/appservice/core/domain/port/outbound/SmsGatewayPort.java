package ir.appservice.core.domain.port.outbound;

import ir.appservice.core.domain.entity.SmsMessage;
import ir.appservice.core.domain.enums.CircuitState;
import ir.appservice.core.domain.enums.SmsProviderType;

/**
 * Outbound Port (SPI): SMS Gateway Communication Contract.
 * Pure Java interface defining SMS delivery, provider switching, and circuit health queries.
 */
public interface SmsGatewayPort {

    /**
     * Dispatches an SMS message payload through configured providers with failover.
     *
     * @param message SMS message entity
     * @return delivery receipt details
     */
    SmsReceipt sendSms(SmsMessage message);

    /**
     * Inspects circuit breaker state for a specific provider.
     *
     * @param provider SMS provider type
     * @return current circuit state
     */
    CircuitState getCircuitState(SmsProviderType provider);

    /**
     * Checks if a provider is healthy and responding.
     *
     * @param provider SMS provider type
     * @return true if provider is active and healthy
     */
    boolean isHealthy(SmsProviderType provider);

    /**
     * SMS delivery receipt record.
     */
    record SmsReceipt(
            boolean delivered,
            String providerMessageId,
            SmsProviderType providerUsed,
            long latencyMs,
            String errorMessage
    ) {
        public static SmsReceipt successful(String providerMessageId, SmsProviderType providerUsed, long latencyMs) {
            return new SmsReceipt(true, providerMessageId, providerUsed, latencyMs, null);
        }

        public static SmsReceipt failed(SmsProviderType providerUsed, String errorMessage) {
            return new SmsReceipt(false, null, providerUsed, 0L, errorMessage);
        }
    }
}
