package ir.appservice.infrastructure.platform.sms;

import ir.appservice.core.domain.enums.CircuitState;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight concurrency-safe Circuit Breaker for individual SMS carrier integrations.
 */
public class SmsCircuitBreaker {

    private final int failureThreshold;
    private final long openCooldownMillis;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private volatile CircuitState state = CircuitState.CLOSED;
    private volatile long lastStateTransitionTimestamp = 0;

    public SmsCircuitBreaker() {
        this(3, 60_000L); // Default: 3 failures trips breaker for 60s
    }

    public SmsCircuitBreaker(int failureThreshold, long openCooldownMillis) {
        this.failureThreshold = Math.max(1, failureThreshold);
        this.openCooldownMillis = openCooldownMillis;
    }

    public synchronized boolean allowExecution() {
        if (state == CircuitState.CLOSED) {
            return true;
        }

        long now = System.currentTimeMillis();
        if (state == CircuitState.OPEN) {
            if (now - lastStateTransitionTimestamp >= openCooldownMillis) {
                state = CircuitState.HALF_OPEN;
                return true;
            }
            return false;
        }

        // HALF_OPEN allows trial call
        return true;
    }

    public synchronized void recordSuccess() {
        consecutiveFailures.set(0);
        state = CircuitState.CLOSED;
    }

    public synchronized void recordFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= failureThreshold) {
            state = CircuitState.OPEN;
            lastStateTransitionTimestamp = System.currentTimeMillis();
        }
    }

    public CircuitState getState() {
        return state;
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures.get();
    }

    public synchronized void reset() {
        consecutiveFailures.set(0);
        state = CircuitState.CLOSED;
    }
}
