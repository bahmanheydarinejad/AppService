package ir.appservice.infrastructure.platform;

import ir.appservice.common.context.TenantContextHolder;
import ir.appservice.common.context.TraceContextHolder;
import ir.appservice.core.domain.entity.AuditEvent;
import ir.appservice.core.domain.enums.Outcome;
import ir.appservice.core.domain.port.outbound.AuditLogPort;
import ir.appservice.infrastructure.platform.audit.AuditLoggerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tamper-Resistant Audit Logger & Secret Masking Tests")
class AuditLoggerServiceTest {

    private final List<AuditEvent> recordedEvents = new ArrayList<>();
    private AuditLogPort auditLogPort;
    private AuditLoggerService auditLoggerService;

    @BeforeEach
    void setUp() {
        recordedEvents.clear();
        auditLogPort = new AuditLogPort() {
            @Override public void recordEvent(AuditEvent event) { recordedEvents.add(event); }
            @Override public List<AuditEvent> findByTraceId(String traceId) { return recordedEvents; }
            @Override public List<AuditEvent> findRecentEvents(int limit, String tenantId) { return recordedEvents; }
            @Override public List<AuditEvent> findByActor(String actor, String tenantId) { return recordedEvents; }
        };

        auditLoggerService = new AuditLoggerService(auditLogPort);
        TraceContextHolder.setTraceId("tr_audit_99182");
        TenantContextHolder.setTenantId("tenant_tehran");
    }

    @AfterEach
    void tearDown() {
        TraceContextHolder.clear();
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("Audit diff automatically masks sensitive fields (password, secretKey, apiKey, token)")
    void testSensitiveFieldsMasked() {
        Map<String, Object> beforeState = Map.of(
                "username", "admin",
                "password", "RawPassword123!",
                "apiKey", "ak_live_secretkey_99",
                "role", "USER"
        );

        Map<String, Object> afterState = Map.of(
                "username", "admin",
                "password", "NewPassword456!",
                "secretKey", "super_secret_token_123",
                "role", "ADMIN"
        );

        AuditEvent event = auditLoggerService.logAction(
                "security_admin",
                "USER_UPDATE",
                "Account",
                "acc_123",
                beforeState,
                afterState,
                Outcome.SUCCESS,
                "192.168.1.100"
        );

        assertThat(event.getTraceId()).isEqualTo("tr_audit_99182");
        assertThat(event.getTenantId()).isEqualTo("tenant_tehran");
        assertThat(recordedEvents).hasSize(1);

        String beforeJson = event.getBeforeState();
        String afterJson = event.getAfterState();

        // Plaintext credentials must NOT be in JSON diffs
        assertThat(beforeJson).doesNotContain("RawPassword123!");
        assertThat(beforeJson).doesNotContain("ak_live_secretkey_99");
        assertThat(beforeJson).contains("\"password\":\"[REDACTED]\"");
        assertThat(beforeJson).contains("\"apiKey\":\"[REDACTED]\"");
        assertThat(beforeJson).contains("\"username\":\"admin\"");

        assertThat(afterJson).doesNotContain("NewPassword456!");
        assertThat(afterJson).doesNotContain("super_secret_token_123");
        assertThat(afterJson).contains("\"password\":\"[REDACTED]\"");
        assertThat(afterJson).contains("\"secretKey\":\"[REDACTED]\"");
        assertThat(afterJson).contains("\"role\":\"ADMIN\"");
    }
}
