package ir.appservice.infrastructure.platform.sms;

import ir.appservice.core.domain.entity.SmsMessage;
import ir.appservice.core.domain.enums.CircuitState;
import ir.appservice.core.domain.enums.SmsProviderType;
import ir.appservice.core.domain.port.outbound.SmsGatewayPort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Intelligent Multi-Provider SMS Gateway Router.
 * Dynamically routes outbound SMS messages with circuit breaker monitoring and automatic failover.
 */
@Component
public class SmartSmsRouter implements SmsGatewayPort {

    private final List<SmsProviderClient> providers = new ArrayList<>();
    private final Map<SmsProviderType, SmsCircuitBreaker> circuitBreakers = new EnumMap<>(SmsProviderType.class);

    public SmartSmsRouter() {
    }

    public SmartSmsRouter(List<SmsProviderClient> clients) {
        if (clients != null) {
            for (SmsProviderClient client : clients) {
                registerProvider(client);
            }
        }
    }

    public synchronized void registerProvider(SmsProviderClient client) {
        providers.add(client);
        circuitBreakers.put(client.getProviderType(), new SmsCircuitBreaker(3, 60_000L));
    }

    @Override
    public SmsReceipt sendSms(SmsMessage message) {
        if (providers.isEmpty()) {
            return SmsReceipt.failed(null, "No SMS provider registered");
        }

        List<String> attemptedErrors = new ArrayList<>();

        for (SmsProviderClient client : providers) {
            SmsProviderType type = client.getProviderType();
            SmsCircuitBreaker breaker = circuitBreakers.computeIfAbsent(type, k -> new SmsCircuitBreaker());

            if (!breaker.allowExecution()) {
                attemptedErrors.add(type + " circuit breaker is OPEN (skipped)");
                continue;
            }

            try {
                SmsReceipt receipt = client.send(message);
                if (receipt != null && receipt.delivered()) {
                    breaker.recordSuccess();
                    return receipt;
                } else {
                    breaker.recordFailure();
                    String err = (receipt != null && receipt.errorMessage() != null) ? receipt.errorMessage() : "Unknown error";
                    attemptedErrors.add(type + " failed: " + err);
                }
            } catch (Exception e) {
                breaker.recordFailure();
                attemptedErrors.add(type + " threw exception: " + e.getMessage());
            }
        }

        return SmsReceipt.failed(null, "All providers failed: " + String.join("; ", attemptedErrors));
    }

    @Override
    public CircuitState getCircuitState(SmsProviderType provider) {
        SmsCircuitBreaker breaker = circuitBreakers.get(provider);
        return breaker != null ? breaker.getState() : CircuitState.CLOSED;
    }

    @Override
    public boolean isHealthy(SmsProviderType provider) {
        return getCircuitState(provider) != CircuitState.OPEN;
    }

    public SmsCircuitBreaker getCircuitBreaker(SmsProviderType provider) {
        return circuitBreakers.get(provider);
    }

    public List<SmsProviderClient> getProviders() {
        return Collections.unmodifiableList(providers);
    }
}
