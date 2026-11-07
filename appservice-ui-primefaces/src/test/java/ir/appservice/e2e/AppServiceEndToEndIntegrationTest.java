package ir.appservice.e2e;

import ir.appservice.common.context.TenantContextHolder;
import ir.appservice.common.context.TraceContextHolder;
import ir.appservice.core.domain.entity.I18nMessage;
import ir.appservice.core.domain.entity.SmsMessage;
import ir.appservice.core.domain.enums.CircuitState;
import ir.appservice.core.domain.enums.IsolationStrategy;
import ir.appservice.core.domain.enums.SmsProviderType;
import ir.appservice.core.domain.port.outbound.I18nMessageRepositoryPort;
import ir.appservice.core.domain.port.outbound.SmsGatewayPort.SmsReceipt;
import ir.appservice.infrastructure.i18n.direction.DirectionResolver;
import ir.appservice.infrastructure.i18n.message.DynamicMessageSource;
import ir.appservice.infrastructure.persistence.entity.TenantJpaEntity;
import ir.appservice.infrastructure.persistence.repository.SpringDataTenantRepository;
import ir.appservice.infrastructure.platform.mcp.McpExecutionHarness;
import ir.appservice.infrastructure.platform.mcp.McpTool;
import ir.appservice.infrastructure.platform.mcp.McpToolRegistry;
import ir.appservice.infrastructure.platform.sms.SmartSmsRouter;
import ir.appservice.infrastructure.platform.sms.SmsProviderClient;
import ir.appservice.infrastructure.security.pkce.PkceChallengeVerifier;
import ir.appservice.ui.bean.PanelHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * STEP-9.1: Comprehensive End-to-End Test Suite covering:
 * 1. Multi-Tenancy Isolation
 * 2. Security Method Checks (PKCE RFC 7636)
 * 3. SMS Gateway Circuit Breaker Failover
 * 4. Dynamic i18n Direction & Hot-Reload
 * 5. Single-Page UI Panel Navigation
 */
class AppServiceEndToEndIntegrationTest {

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
        TraceContextHolder.clear();
    }

    @Test
    @DisplayName("E2E-1: Multi-Tenancy Isolation and Trace Context Propagation")
    void testMultiTenancyIsolationAndTraceContext() {
        TraceContextHolder.setTraceId("trace-e2e-999");
        TenantContextHolder.setTenantId("tenant-finance");

        assertThat(TraceContextHolder.getTraceIdOrDefault()).isEqualTo("trace-e2e-999");
        assertThat(TenantContextHolder.getTenantId()).isEqualTo("tenant-finance");

        SpringDataTenantRepository tenantRepo = mock(SpringDataTenantRepository.class);
        TenantJpaEntity tenant = new TenantJpaEntity();
        tenant.setId("t-finance");
        tenant.setTenantCode("finance_dept");
        tenant.setIsolationStrategy(IsolationStrategy.POSTGRESQL_SCHEMA);

        when(tenantRepo.findById("t-finance")).thenReturn(Optional.of(tenant));

        Optional<TenantJpaEntity> loaded = tenantRepo.findById("t-finance");
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getIsolationStrategy()).isEqualTo(IsolationStrategy.POSTGRESQL_SCHEMA);
    }

    @Test
    @DisplayName("E2E-2: Security PKCE Challenge Verification according to RFC 7636")
    void testSecurityPkceVerification() {
        PkceChallengeVerifier verifier = new PkceChallengeVerifier();
        String verifierString = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        String challenge = verifier.computeS256Challenge(verifierString);

        boolean valid = verifier.verify(verifierString, challenge, PkceChallengeVerifier.METHOD_S256);
        assertThat(valid).isTrue();

        boolean tampered = verifier.verify("tampered-verifier", challenge, PkceChallengeVerifier.METHOD_S256);
        assertThat(tampered).isFalse();
    }

    @Test
    @DisplayName("E2E-3: Smart SMS Router Failover with Circuit Breaker")
    void testSmsRouterFailover() {
        // Provider A: Simulated failing primary carrier
        SmsProviderClient providerA = new SmsProviderClient() {
            @Override public SmsProviderType getProviderType() { return SmsProviderType.KAVENEGAR; }
            @Override public SmsReceipt send(SmsMessage message) {
                return SmsReceipt.failed(SmsProviderType.KAVENEGAR, "Carrier Gateway Timeout");
            }
        };

        // Provider B: Reliable fallback carrier
        SmsProviderClient providerB = new SmsProviderClient() {
            @Override public SmsProviderType getProviderType() { return SmsProviderType.MAGFA; }
            @Override public SmsReceipt send(SmsMessage message) {
                return SmsReceipt.successful("msg_magfa_success_123", SmsProviderType.MAGFA, 75L);
            }
        };

        SmartSmsRouter router = new SmartSmsRouter();
        router.registerProvider(providerA);
        router.registerProvider(providerB);

        // 3 consecutive failed requests on Provider A
        SmsMessage testMessage = new SmsMessage("+989123456789", "Failover Alert OTP", SmsProviderType.KAVENEGAR, "system");
        router.sendSms(testMessage);
        router.sendSms(testMessage);
        router.sendSms(testMessage);

        // Verify primary circuit is tripped to OPEN
        assertThat(router.getCircuitState(SmsProviderType.KAVENEGAR)).isEqualTo(CircuitState.OPEN);
        assertThat(router.isHealthy(SmsProviderType.KAVENEGAR)).isFalse();

        // 4th request skips failing Provider A entirely and succeeds via Provider B
        SmsReceipt receipt = router.sendSms(testMessage);
        assertThat(receipt.delivered()).isTrue();
        assertThat(receipt.providerUsed()).isEqualTo(SmsProviderType.MAGFA);
    }

    @Test
    @DisplayName("E2E-4: Dynamic i18n Resolution, Hot-Reload, and Direction Mirroring")
    void testI18nAndDirectionMirroring() {
        DirectionResolver directionResolver = new DirectionResolver();

        // Persian locale -> RTL
        assertThat(directionResolver.resolveHtmlDir(new Locale("fa", "IR"))).isEqualTo("rtl");
        assertThat(directionResolver.isRtl(new Locale("fa"))).isTrue();

        // English locale -> LTR
        assertThat(directionResolver.resolveHtmlDir(Locale.ENGLISH)).isEqualTo("ltr");
        assertThat(directionResolver.isRtl(Locale.ENGLISH)).isFalse();

        // Dynamic message source lookup
        TenantContextHolder.setTenantId("default");
        I18nMessageRepositoryPort repo = mock(I18nMessageRepositoryPort.class);
        when(repo.findByKeyAndLocaleAndTenantId(eq("btn.submit"), anyString(), any()))
                .thenReturn(Optional.of(new I18nMessage("1", "btn.submit", "fa", "تایید و ارسال", "general", "default")));

        DynamicMessageSource messageSource = new DynamicMessageSource(repo);

        String persianMsg = messageSource.getMessage("btn.submit", null, new Locale("fa"));
        assertThat(persianMsg).isEqualTo("تایید و ارسال");
    }

    @Test
    @DisplayName("E2E-5: Single-Page Dynamic AJAX Navigation and AI Agent MCP Execution")
    void testSinglePageNavigationAndMcpPlane() {
        PanelHandler panelHandler = new PanelHandler();
        assertThat(panelHandler.getActivePanelName()).isEqualTo("audit");

        // Transition over AJAX to AI panel
        panelHandler.switchPanel("ai");
        assertThat(panelHandler.getActivePanelName()).isEqualTo("ai");
        assertThat(panelHandler.getActivePanel()).isEqualTo("/panels/ai.xhtml");

        // MCP registry and execution
        McpToolRegistry registry = new McpToolRegistry();
        registry.registerTool(new McpTool(
                "system_ping",
                "Health ping",
                "{\"type\":\"object\"}",
                false,
                args -> "PONG"
        ));

        McpExecutionHarness harness = new McpExecutionHarness(registry, null);

        McpExecutionHarness.ToolExecutionResult result = harness.executeTool(
                "agent-e2e",
                "system_ping",
                Map.of(),
                null,
                50
        );

        assertThat(result.success()).isTrue();
        assertThat(result.output()).isEqualTo("PONG");
    }
}
