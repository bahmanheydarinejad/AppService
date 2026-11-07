package ir.appservice.core.domain.enums;

/**
 * Resilience circuit breaker states for external gateways (e.g. SMS, SMTP, Payment).
 */
public enum CircuitState {
    /**
     * Normal operational state: requests flow through freely.
     */
    CLOSED,

    /**
     * Tripped state: consecutive failures exceeded threshold; requests fail-fast or fallback.
     */
    OPEN,

    /**
     * Trial probe state: limited test requests are allowed to determine upstream recovery.
     */
    HALF_OPEN,

    /**
     * Circuit breaker is disabled: all requests pass unmonitored.
     */
    DISABLED,

    /**
     * Manually forced open: administrative circuit isolation.
     */
    FORCED_OPEN;

    public boolean allowsExecution() {
        return this == CLOSED || this == HALF_OPEN || this == DISABLED;
    }

    public boolean isTripped() {
        return this == OPEN || this == FORCED_OPEN;
    }
}
