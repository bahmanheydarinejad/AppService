package ir.appservice.core.domain;

import ir.appservice.core.domain.entity.AuditEvent;
import ir.appservice.core.domain.entity.I18nMessage;
import ir.appservice.core.domain.entity.Permission;
import ir.appservice.core.domain.entity.Role;
import ir.appservice.core.domain.entity.ScheduledJob;
import ir.appservice.core.domain.entity.SmsMessage;
import ir.appservice.core.domain.entity.Tenant;
import ir.appservice.core.domain.entity.User;
import ir.appservice.core.domain.enums.IsolationStrategy;
import ir.appservice.core.domain.enums.Outcome;
import ir.appservice.core.domain.enums.SmsProviderType;
import ir.appservice.core.domain.enums.Status;
import ir.appservice.core.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Domain Entities Business Logic & Invariants Tests")
class EntitiesBusinessLogicTest {

    @Test
    @DisplayName("User: verify RBAC, permission hierarchy, failed logins, and status locking")
    void testUserEntity() {
        User user = new User("usr_1", "admin_sys", "hashed_pwd", "admin@appservice.ir", "tenant_a");
        user.setFirstName("Bahman");
        user.setLastName("Heydari");

        Permission userRead = new Permission("perm_1", "USERS_VIEW", "View Users", "USER_MGMT");
        Permission userWrite = new Permission("perm_2", "USERS_MANAGE", "Manage Users", "USER_MGMT");

        Role adminRole = new Role("role_1", "ROLE_ADMIN", "Administrator", "tenant_a");
        adminRole.addPermission(userRead);
        adminRole.addPermission(userWrite);

        user.addRole(adminRole);

        assertThat(user.hasRole("ROLE_ADMIN")).isTrue();
        assertThat(user.hasRole("ROLE_GUEST")).isFalse();
        assertThat(user.hasPermission("USERS_VIEW")).isTrue();
        assertThat(user.hasPermission("USERS_MANAGE")).isTrue();
        assertThat(user.hasPermission("TENANTS_DELETE")).isFalse();

        // Test login failures and auto-lock
        user.recordFailedLogin(3);
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(user.getStatus()).isEqualTo(Status.ACTIVE);

        user.recordFailedLogin(3);
        user.recordFailedLogin(3);
        assertThat(user.getFailedLoginAttempts()).isEqualTo(3);
        assertThat(user.getStatus()).isEqualTo(Status.LOCKED);

        // Reset on success
        user.activate();
        user.recordSuccessfulLogin();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(0);
        assertThat(user.getLastLoginAt()).isNotNull();

        // Invariant violations
        assertThatThrownBy(() -> new User("u1", null, "pwd", "e@e.com", "t1"))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Tenant: verify quota checks, kill-switch, and isolation configurations")
    void testTenantEntity() {
        Tenant tenant = new Tenant("tnt_1", "corp_alpha", "Alpha Corporation", IsolationStrategy.POSTGRESQL_SCHEMA);
        tenant.setSchemaName("tenant_corp_alpha");
        tenant.setQuotaUsers(50);
        tenant.setQuotaStorageMb(2048);

        assertThat(tenant.canAddUsers(49)).isTrue();
        assertThat(tenant.canAddUsers(50)).isFalse();
        assertThat(tenant.canStoreMb(2000)).isTrue();
        assertThat(tenant.canStoreMb(2048)).isFalse();

        // Kill switch activation blocks operations immediately
        tenant.triggerKillSwitch();
        assertThat(tenant.isKillSwitchActive()).isTrue();
        assertThat(tenant.canAddUsers(10)).isFalse();
        assertThat(tenant.canStoreMb(100)).isFalse();

        tenant.resetKillSwitch();
        assertThat(tenant.isKillSwitchActive()).isFalse();
        assertThat(tenant.canAddUsers(10)).isTrue();

        // Invariants
        assertThatThrownBy(() -> new Tenant("t1", "   ", "Name", IsolationStrategy.SHARED_DB))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("AuditEvent: verify immutable audit recording and action validation")
    void testAuditEventEntity() {
        AuditEvent event = new AuditEvent(
                "tr-12345",
                "admin",
                "USER_UPDATE",
                "User:usr_100",
                "{\"status\": [\"ACTIVE\", \"SUSPENDED\"]}",
                Outcome.SUCCESS,
                "127.0.0.1",
                "tenant_a"
        );

        assertThat(event.getId()).isNotBlank();
        assertThat(event.getTraceId()).isEqualTo("tr-12345");
        assertThat(event.getAction()).isEqualTo("USER_UPDATE");
        assertThat(event.getOutcome()).isEqualTo(Outcome.SUCCESS);
        assertThat(event.getTimestamp()).isNotNull();

        assertThatThrownBy(() -> new AuditEvent("tr-1", "user", "  ", "subject", null, Outcome.SUCCESS, null, null))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("I18nMessage: verify dynamic localization and versioning")
    void testI18nMessageEntity() {
        I18nMessage message = new I18nMessage("msg_1", "button.submit", "fa", "ارسال", "ui", "tenant_a");
        assertThat(message.getVersion()).isEqualTo(1L);
        assertThat(message.getContent()).isEqualTo("ارسال");

        message.updateContent("ثبت اطلاعات");
        assertThat(message.getVersion()).isEqualTo(2L);
        assertThat(message.getContent()).isEqualTo("ثبت اطلاعات");

        assertThatThrownBy(() -> new I18nMessage("m1", null, "fa", "test", "ui", "t1"))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("SmsMessage: verify delivery transition, latency tracking, and retries")
    void testSmsMessageEntity() {
        SmsMessage sms = new SmsMessage("+989121234567", "Your verification code is 48291", SmsProviderType.KAVENEGAR, "tenant_a");
        assertThat(sms.getStatus()).isEqualTo(Status.PENDING);
        assertThat(sms.getRetryCount()).isEqualTo(0);

        sms.incrementRetry();
        assertThat(sms.getRetryCount()).isEqualTo(1);

        sms.markDelivered(240L);
        assertThat(sms.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(sms.getLatencyMs()).isEqualTo(240L);
        assertThat(sms.getDeliveredAt()).isNotNull();

        assertThatThrownBy(() -> new SmsMessage("", "text", SmsProviderType.MOCK, "t1"))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("ScheduledJob: verify execution metadata and pause/resume lifecycle")
    void testScheduledJobEntity() {
        ScheduledJob job = new ScheduledJob("job_1", "AuditCleanupJob", "MAINTENANCE", "0 0 2 * * ?", "default");
        assertThat(job.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(job.getExecutionCount()).isEqualTo(0L);

        Instant execTime = Instant.now();
        Instant nextTime = execTime.plusSeconds(86400);
        job.recordExecution(execTime, nextTime, "inst_worker_1");

        assertThat(job.getExecutionCount()).isEqualTo(1L);
        assertThat(job.getLastExecutionTime()).isEqualTo(execTime);
        assertThat(job.getNextExecutionTime()).isEqualTo(nextTime);
        assertThat(job.getFireInstanceId()).isEqualTo("inst_worker_1");

        job.pause();
        assertThat(job.getStatus()).isEqualTo(Status.SUSPENDED);

        job.resume();
        assertThat(job.getStatus()).isEqualTo(Status.ACTIVE);
    }
}
