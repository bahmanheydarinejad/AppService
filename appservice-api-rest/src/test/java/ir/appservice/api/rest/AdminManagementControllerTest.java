package ir.appservice.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.appservice.api.rest.controller.*;
import ir.appservice.core.domain.entity.ScheduledJob;
import ir.appservice.core.domain.entity.SmsMessage;
import ir.appservice.core.domain.enums.CircuitState;
import ir.appservice.core.domain.enums.Outcome;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminManagementControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("STEP-7.3: AuditManagementController returns sanitized diff with masked secrets")
    void testAuditDiffMasking() throws Exception {
        SpringDataAuditLogRepository repo = mock(SpringDataAuditLogRepository.class);
        AuditDiffSanitizer sanitizer = new AuditDiffSanitizer();

        AuditEventJpaEntity event = new AuditEventJpaEntity();
        event.setId("evt-123");
        event.setActionVerb("UPDATE_USER");
        event.setBeforeState("{\"password\":\"plainSecret\",\"email\":\"user@appservice.internal\"}");
        event.setAfterState("{\"password\":\"newSecret\",\"email\":\"user@appservice.internal\"}");

        when(repo.findById("evt-123")).thenReturn(Optional.of(event));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new AuditManagementController(repo, sanitizer)).build();

        mvc.perform(get("/api/admin/audit-events/evt-123/diff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.beforeState").value(org.hamcrest.Matchers.containsString("[REDACTED]")))
                .andExpect(jsonPath("$.data.afterState").value(org.hamcrest.Matchers.containsString("[REDACTED]")));
    }

    @Test
    @DisplayName("STEP-7.3: SchedulerManagementController fires, pauses, resumes, and lists jobs")
    void testSchedulerManagement() throws Exception {
        PlatformJobSchedulerService scheduler = mock(PlatformJobSchedulerService.class);
        ScheduledJob job = new ScheduledJob("job-1", "cleanTemp", "MAINTENANCE", "0 0 * * * ?", "system");
        when(scheduler.listJobs()).thenReturn(List.of(job));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SchedulerManagementController(scheduler)).build();

        mvc.perform(get("/api/admin/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].jobName").value("cleanTemp"));

        mvc.perform(post("/api/admin/jobs/MAINTENANCE:cleanTemp/fire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        verify(scheduler).fireNow("cleanTemp", "MAINTENANCE");

        mvc.perform(post("/api/admin/jobs/MAINTENANCE:cleanTemp/pause"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        verify(scheduler).pauseJob("cleanTemp", "MAINTENANCE");

        mvc.perform(post("/api/admin/jobs/MAINTENANCE:cleanTemp/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        verify(scheduler).resumeJob("cleanTemp", "MAINTENANCE");
    }

    @Test
    @DisplayName("STEP-7.3: SmsManagementController manages carriers, checks health, updates routing, and sends SMS")
    void testSmsManagement() throws Exception {
        SmartSmsRouter router = mock(SmartSmsRouter.class);
        when(router.isHealthy(any(SmsProviderType.class))).thenReturn(true);
        when(router.getCircuitState(any(SmsProviderType.class))).thenReturn(CircuitState.CLOSED);
        when(router.sendSms(any(SmsMessage.class)))
                .thenReturn(new SmsGatewayPort.SmsReceipt(true, "msg-123", SmsProviderType.KAVENEGAR, 45L, null));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SmsManagementController(router)).build();

        mvc.perform(get("/api/admin/sms/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mvc.perform(get("/api/admin/sms/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.primaryHealthy").value(true));

        mvc.perform(put("/api/admin/sms/routing-policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policy\":\"LATENCY_OPTIMIZED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("LATENCY_OPTIMIZED"));

        mvc.perform(post("/api/admin/sms/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipient\":\"+989123456789\",\"content\":\"Test OTP\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.delivered").value(true));
    }

    @Test
    @DisplayName("STEP-7.3: MailManagementController renders templates and dispatches test emails")
    void testMailManagement() throws Exception {
        PlatformMailEngine mailEngine = mock(PlatformMailEngine.class);
        MailTemplateEngine templateEngine = new MailTemplateEngine();

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new MailManagementController(mailEngine, templateEngine)).build();

        mvc.perform(get("/api/admin/mail/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mvc.perform(post("/api/admin/mail/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"template\":\"Hello {{name}}!\",\"model\":{\"name\":\"John\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Hello John!"));

        mvc.perform(post("/api/admin/mail/test-send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"to\":\"john@example.com\",\"subject\":\"Hi\",\"body\":\"Test body\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        verify(mailEngine).sendHtmlEmail("john@example.com", "Hi", "Test body");
    }

    @Test
    @DisplayName("STEP-7.3: OpsMonitoringController exposes metrics, pools, and circuits")
    void testOpsMonitoring() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new OpsMonitoringController()).build();

        mvc.perform(get("/api/admin/ops/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.p50LatencyMs").isNumber());

        mvc.perform(get("/api/admin/ops/pools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.poolName").value("HikariPool-1"));

        mvc.perform(get("/api/admin/ops/circuits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kavenegarCircuit").value("CLOSED"));
    }

    @Test
    @DisplayName("STEP-7.3: TenantManagementController updates strategy and provisions tenant")
    void testTenantManagement() throws Exception {
        SpringDataTenantRepository repo = mock(SpringDataTenantRepository.class);
        TenantJpaEntity tenant = new TenantJpaEntity();
        tenant.setId("t-1");
        tenant.setName("Acme");
        when(repo.findById("t-1")).thenReturn(Optional.of(tenant));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new TenantManagementController(repo)).build();

        mvc.perform(put("/api/admin/tenants/t-1/strategy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"strategy\":\"POSTGRESQL_SCHEMA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        verify(repo).save(tenant);

        mvc.perform(post("/api/admin/tenants/t-1/provision"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PROVISIONED"));
    }

    @Test
    @DisplayName("STEP-7.3: OAuth2ClientManagementController registers client and rotates secret")
    void testOAuth2ClientManagement() throws Exception {
        OAuth2ClientService clientService = mock(OAuth2ClientService.class);
        when(clientService.registerClient(anyString(), any(), any(), any(), anyBoolean(), anyInt(), anyString()))
                .thenReturn(new OAuth2ClientService.ClientRegistrationResult("c-123", "rawSec123", null));
        when(clientService.rotateSecret("c-123"))
                .thenReturn(Optional.of(new OAuth2ClientService.ClientRegistrationResult("c-123", "rotatedSec456", null)));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new OAuth2ClientManagementController(clientService)).build();

        mvc.perform(post("/api/admin/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clientName\":\"CustomApp\",\"requirePkce\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.clientId").value("c-123"))
                .andExpect(jsonPath("$.data.rawClientSecret").value("rawSec123"));

        mvc.perform(post("/api/admin/clients/c-123/keys/rotate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rawClientSecret").value("rotatedSec456"));
    }

    @Test
    @DisplayName("STEP-7.3: McpAgentController lists agents, tools, and invokes tool")
    void testMcpAgentPlane() throws Exception {
        McpExecutionHarness harness = mock(McpExecutionHarness.class);
        McpToolRegistry registry = new McpToolRegistry();
        registry.registerTool(new McpTool("db_query", "Run read-only query", "{\"type\":\"object\"}", false, params -> "Rows: 5"));

        when(harness.executeTool(anyString(), anyString(), any(), any(), anyInt()))
                .thenReturn(McpExecutionHarness.ToolExecutionResult.success("Rows: 5", 25));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new McpAgentController(harness, registry)).build();

        mvc.perform(get("/api/admin/ai/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mvc.perform(get("/api/admin/ai/mcp/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("db_query"));

        mvc.perform(post("/api/admin/ai/mcp/invoke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toolName\":\"db_query\",\"parameters\":{\"sql\":\"SELECT 1\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(true));
    }
}
