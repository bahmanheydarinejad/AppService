package ir.appservice.ui.bean;

import ir.appservice.core.domain.entity.ScheduledJob;
import ir.appservice.core.domain.entity.SmsMessage;
import ir.appservice.core.domain.enums.CircuitState;
import ir.appservice.core.domain.enums.IsolationStrategy;
import ir.appservice.core.domain.enums.SmsProviderType;
import ir.appservice.core.domain.port.outbound.SmsGatewayPort;
import ir.appservice.infrastructure.persistence.entity.AuditEventJpaEntity;
import ir.appservice.infrastructure.persistence.entity.TenantJpaEntity;
import ir.appservice.infrastructure.persistence.repository.SpringDataAuditLogRepository;
import ir.appservice.infrastructure.persistence.repository.SpringDataTenantRepository;
import ir.appservice.infrastructure.platform.audit.AuditDiffSanitizer;
import ir.appservice.infrastructure.platform.mail.MailTemplateEngine;
import ir.appservice.infrastructure.platform.mail.PlatformMailEngine;
import ir.appservice.infrastructure.platform.mcp.McpExecutionHarness;
import ir.appservice.infrastructure.platform.mcp.McpTool;
import ir.appservice.infrastructure.platform.mcp.McpToolRegistry;
import ir.appservice.infrastructure.platform.scheduler.PlatformJobSchedulerService;
import ir.appservice.infrastructure.platform.sms.SmartSmsRouter;
import ir.appservice.infrastructure.security.oauth2.OAuth2ClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AdminBackingBeansTest {

    @Test
    @DisplayName("STEP-8.4: AuditLogBean filters by trace ID and sanitizes diffs")
    void testAuditLogBean() {
        SpringDataAuditLogRepository repo = mock(SpringDataAuditLogRepository.class);
        AuditDiffSanitizer sanitizer = new AuditDiffSanitizer();

        AuditEventJpaEntity event = new AuditEventJpaEntity();
        event.setTraceId("tr-100");
        event.setActionVerb("USER_UPDATE");
        event.setBeforeState("{\"password\":\"plainSecret\"}");
        event.setAfterState("{\"password\":\"newSecret\"}");

        when(repo.findByTraceIdOrderByTimestampDesc("tr-100")).thenReturn(List.of(event));

        AuditLogBean bean = new AuditLogBean(repo, sanitizer);
        bean.setTraceIdFilter("tr-100");
        bean.filterEvents();

        assertThat(bean.getAuditEvents()).hasSize(1);

        bean.selectEvent(event);
        assertThat(bean.getSanitizedBeforeState()).contains("[REDACTED]");
        assertThat(bean.getSanitizedAfterState()).contains("[REDACTED]");
    }

    @Test
    @DisplayName("STEP-8.5: JobSchedulerBean fires, pauses, and resumes jobs")
    void testJobSchedulerBean() throws Exception {
        PlatformJobSchedulerService scheduler = mock(PlatformJobSchedulerService.class);
        ScheduledJob job = new ScheduledJob("j-1", "cleanTemp", "SYSTEM", "0 0 * * * ?", "system");
        when(scheduler.listJobs()).thenReturn(List.of(job));

        JobSchedulerBean bean = new JobSchedulerBean(scheduler);
        bean.loadJobs();
        assertThat(bean.getJobs()).hasSize(1);

        bean.fireNow(job);
        verify(scheduler).fireNow("cleanTemp", "SYSTEM");

        bean.pauseJob(job);
        verify(scheduler).pauseJob("cleanTemp", "SYSTEM");

        bean.resumeJob(job);
        verify(scheduler).resumeJob("cleanTemp", "SYSTEM");
    }

    @Test
    @DisplayName("STEP-8.6: MailManagementBean renders preview and sends test email")
    void testMailManagementBean() {
        PlatformMailEngine mailEngine = mock(PlatformMailEngine.class);
        MailTemplateEngine templateEngine = new MailTemplateEngine();

        MailManagementBean bean = new MailManagementBean(mailEngine, templateEngine);
        bean.setTemplateSource("Hello {{name}}");
        bean.renderPreview();
        assertThat(bean.getPreviewHtml()).contains("Hello System Administrator");

        bean.sendTestEmail();
        verify(mailEngine).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("STEP-8.7: SmsManagementBean configures routing policy and dispatches test SMS")
    void testSmsManagementBean() {
        SmartSmsRouter router = mock(SmartSmsRouter.class);
        when(router.isHealthy(SmsProviderType.KAVENEGAR)).thenReturn(true);
        when(router.getCircuitState(SmsProviderType.KAVENEGAR)).thenReturn(CircuitState.CLOSED);
        when(router.sendSms(any(SmsMessage.class)))
                .thenReturn(new SmsGatewayPort.SmsReceipt(true, "rec-123", SmsProviderType.KAVENEGAR, 35L, null));

        SmsManagementBean bean = new SmsManagementBean(router);
        bean.updateRoutingPolicy("LATENCY_OPTIMIZED");
        assertThat(bean.getRoutingPolicy()).isEqualTo("LATENCY_OPTIMIZED");

        assertThat(bean.isProviderHealthy("KAVENEGAR")).isTrue();
        assertThat(bean.getCircuitState("KAVENEGAR")).isEqualTo("CLOSED");

        bean.sendTestSms();
        assertThat(bean.getLastReceipt()).isNotNull();
        assertThat(bean.getLastReceipt().delivered()).isTrue();
    }

    @Test
    @DisplayName("STEP-8.8: OpsMonitoringBean retrieves latency and pool metrics")
    void testOpsMonitoringBean() {
        OpsMonitoringBean bean = new OpsMonitoringBean();
        bean.refreshMetrics();

        assertThat(bean.getP50LatencyMs()).isEqualTo(4.2);
        assertThat(bean.getHikariTotal()).isEqualTo(20);
        assertThat(bean.getHeapMaxMb()).isGreaterThan(0L);
    }

    @Test
    @DisplayName("STEP-8.9: TenantManagementBean provisions tenants and updates strategy")
    void testTenantManagementBean() {
        SpringDataTenantRepository repo = mock(SpringDataTenantRepository.class);
        TenantJpaEntity tenant = new TenantJpaEntity();
        tenant.setTenantCode("corp_alpha");
        when(repo.findAll()).thenReturn(List.of(tenant));

        TenantManagementBean bean = new TenantManagementBean(repo);
        assertThat(bean.getTenants()).hasSize(1);

        bean.setNewTenantCode("corp_beta");
        bean.setSelectedStrategy(IsolationStrategy.POSTGRESQL_SCHEMA);
        bean.provisionTenant();
        verify(repo).save(any(TenantJpaEntity.class));

        bean.updateStrategy(tenant, IsolationStrategy.DEDICATED_DB);
        assertThat(tenant.getIsolationStrategy()).isEqualTo(IsolationStrategy.DEDICATED_DB);
        verify(repo, atLeastOnce()).save(tenant);
    }

    @Test
    @DisplayName("STEP-8.10: OAuth2ClientBean registers clients and rotates secret")
    void testOAuth2ClientBean() {
        OAuth2ClientService clientService = mock(OAuth2ClientService.class);
        when(clientService.registerClient(anyString(), any(), any(), any(), anyBoolean(), anyInt(), anyString()))
                .thenReturn(new OAuth2ClientService.ClientRegistrationResult("c-999", "rawSecretVal", null));
        when(clientService.rotateSecret("c-999"))
                .thenReturn(Optional.of(new OAuth2ClientService.ClientRegistrationResult("c-999", "newRotatedVal", null)));

        OAuth2ClientBean bean = new OAuth2ClientBean(clientService);
        bean.registerClient();
        assertThat(bean.getCreatedClientId()).isEqualTo("c-999");
        assertThat(bean.getCreatedClientSecret()).isEqualTo("rawSecretVal");

        bean.rotateSecret("c-999");
        assertThat(bean.getRotatedClientSecret()).isEqualTo("newRotatedVal");
    }

    @Test
    @DisplayName("STEP-8.11: AiAgentPanelBean discovers MCP tools and invokes tool")
    void testAiAgentPanelBean() {
        McpExecutionHarness harness = mock(McpExecutionHarness.class);
        McpToolRegistry registry = new McpToolRegistry();
        registry.registerTool(new McpTool("query_logs", "Query logs", "{}", false, p -> "Logs: 10 items"));

        when(harness.executeTool(anyString(), anyString(), any(), any(), anyInt()))
                .thenReturn(McpExecutionHarness.ToolExecutionResult.success("Logs: 10 items", 50));

        AiAgentPanelBean bean = new AiAgentPanelBean(harness, registry);
        assertThat(bean.getTools()).hasSize(1);
        assertThat(bean.getTools().get(0).name()).isEqualTo("query_logs");

        bean.setSelectedToolName("query_logs");
        bean.invokeSelectedTool();
        assertThat(bean.getLastExecutionOutput()).contains("Logs: 10 items");
    }
}
