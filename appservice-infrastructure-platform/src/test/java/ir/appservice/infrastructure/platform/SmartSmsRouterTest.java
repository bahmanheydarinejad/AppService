package ir.appservice.infrastructure.platform;

import ir.appservice.core.domain.entity.SmsMessage;
import ir.appservice.core.domain.enums.CircuitState;
import ir.appservice.core.domain.enums.SmsProviderType;
import ir.appservice.core.domain.port.outbound.SmsGatewayPort.SmsReceipt;
import ir.appservice.infrastructure.platform.sms.SmartSmsRouter;
import ir.appservice.infrastructure.platform.sms.SmsProviderClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Intelligent SMS Gateway & Circuit Breaker Failover Tests")
class SmartSmsRouterTest {

    @Test
    @DisplayName("STEP-6.2 Proof: 3 consecutive timeouts on Provider A trigger instant fallback to Provider B")
    void testCircuitBreakerFailover() {
        AtomicInteger providerACalls = new AtomicInteger(0);
        AtomicInteger providerBCalls = new AtomicInteger(0);

        // Provider A: Simulated failing carrier
        SmsProviderClient providerA = new SmsProviderClient() {
            @Override public SmsProviderType getProviderType() { return SmsProviderType.KAVENEGAR; }
            @Override public SmsReceipt send(SmsMessage message) {
                providerACalls.incrementAndGet();
                return SmsReceipt.failed(SmsProviderType.KAVENEGAR, "Carrier Gateway Timeout");
            }
        };

        // Provider B: Reliable fallback carrier
        SmsProviderClient providerB = new SmsProviderClient() {
            @Override public SmsProviderType getProviderType() { return SmsProviderType.MAGFA; }
            @Override public SmsReceipt send(SmsMessage message) {
                providerBCalls.incrementAndGet();
                return SmsReceipt.successful("msg_magfa_success_123", SmsProviderType.MAGFA, 85L);
            }
        };

        SmartSmsRouter router = new SmartSmsRouter();
        router.registerProvider(providerA);
        router.registerProvider(providerB);

        SmsMessage testMessage = new SmsMessage("+989121234567", "Your verification code is 4492", SmsProviderType.KAVENEGAR, "tenant_default");

        // Request 1: Provider A fails, router falls back to Provider B
        SmsReceipt receipt1 = router.sendSms(testMessage);
        assertThat(receipt1.delivered()).isTrue();
        assertThat(receipt1.providerUsed()).isEqualTo(SmsProviderType.MAGFA);
        assertThat(providerACalls.get()).isEqualTo(1);
        assertThat(providerBCalls.get()).isEqualTo(1);
        assertThat(router.getCircuitState(SmsProviderType.KAVENEGAR)).isEqualTo(CircuitState.CLOSED);

        // Request 2: Provider A fails second time
        SmsReceipt receipt2 = router.sendSms(testMessage);
        assertThat(receipt2.delivered()).isTrue();
        assertThat(providerACalls.get()).isEqualTo(2);

        // Request 3: Provider A fails third consecutive time -> trips breaker to OPEN
        SmsReceipt receipt3 = router.sendSms(testMessage);
        assertThat(receipt3.delivered()).isTrue();
        assertThat(providerACalls.get()).isEqualTo(3);
        assertThat(router.getCircuitState(SmsProviderType.KAVENEGAR)).isEqualTo(CircuitState.OPEN);

        // Request 4: Provider A circuit is OPEN! Call must bypass Provider A directly to Provider B
        int providerABefore = providerACalls.get();
        int providerBBefore = providerBCalls.get();

        SmsReceipt receipt4 = router.sendSms(testMessage);
        assertThat(receipt4.delivered()).isTrue();
        assertThat(receipt4.providerUsed()).isEqualTo(SmsProviderType.MAGFA);

        // Provider A was NOT even called because its circuit breaker is OPEN!
        assertThat(providerACalls.get()).isEqualTo(providerABefore);
        assertThat(providerBCalls.get()).isEqualTo(providerBBefore + 1);
    }
}
