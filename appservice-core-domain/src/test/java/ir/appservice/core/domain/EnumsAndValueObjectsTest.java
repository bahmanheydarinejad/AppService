package ir.appservice.core.domain;

import ir.appservice.core.domain.enums.CircuitState;
import ir.appservice.core.domain.enums.IsolationStrategy;
import ir.appservice.core.domain.enums.Outcome;
import ir.appservice.core.domain.enums.SmsProviderType;
import ir.appservice.core.domain.enums.Status;
import ir.appservice.core.domain.exception.DomainValidationException;
import ir.appservice.core.domain.valueobject.EmailAddress;
import ir.appservice.core.domain.valueobject.PhoneNumber;
import ir.appservice.core.domain.valueobject.TenantId;
import ir.appservice.core.domain.valueobject.TraceId;
import ir.appservice.core.domain.valueobject.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Domain Enums and Value Objects Contract Tests")
class EnumsAndValueObjectsTest {

    @Test
    @DisplayName("Status: verify lifecycle state transitions and invariants")
    void testStatusTransitions() {
        assertThat(Status.ACTIVE.isActive()).isTrue();
        assertThat(Status.INACTIVE.isActive()).isFalse();
        assertThat(Status.DELETED.isTerminal()).isTrue();

        // Valid transitions
        assertThat(Status.PENDING.canTransitionTo(Status.ACTIVE)).isTrue();
        assertThat(Status.ACTIVE.canTransitionTo(Status.SUSPENDED)).isTrue();
        assertThat(Status.ACTIVE.canTransitionTo(Status.LOCKED)).isTrue();
        assertThat(Status.ACTIVE.canTransitionTo(Status.DELETED)).isTrue();
        assertThat(Status.SUSPENDED.canTransitionTo(Status.ACTIVE)).isTrue();

        // Invalid transitions
        assertThat(Status.DELETED.canTransitionTo(Status.ACTIVE)).isFalse();
        assertThat(Status.DELETED.canTransitionTo(Status.PENDING)).isFalse();
        assertThat(Status.ACTIVE.canTransitionTo(null)).isFalse();
    }

    @Test
    @DisplayName("Outcome: verify success, failure, and alerting logic")
    void testOutcomeContracts() {
        assertThat(Outcome.SUCCESS.isSuccess()).isTrue();
        assertThat(Outcome.SUCCESS.isFailure()).isFalse();
        assertThat(Outcome.SUCCESS.requiresAlert()).isFalse();

        assertThat(Outcome.FAILURE.isSuccess()).isFalse();
        assertThat(Outcome.FAILURE.isFailure()).isTrue();
        assertThat(Outcome.FAILURE.requiresAlert()).isTrue();

        assertThat(Outcome.BLOCKED.isFailure()).isTrue();
        assertThat(Outcome.BLOCKED.requiresAlert()).isTrue();

        assertThat(Outcome.TIMEOUT.isFailure()).isTrue();
    }

    @Test
    @DisplayName("IsolationStrategy: verify tiers and provisioning flags")
    void testIsolationStrategy() {
        assertThat(IsolationStrategy.SHARED_DB.getTierLevel()).isEqualTo(1);
        assertThat(IsolationStrategy.SHARED_DB.requiresSchemaCreation()).isFalse();
        assertThat(IsolationStrategy.SHARED_DB.requiresDedicatedDatasource()).isFalse();

        assertThat(IsolationStrategy.POSTGRESQL_SCHEMA.getTierLevel()).isEqualTo(2);
        assertThat(IsolationStrategy.POSTGRESQL_SCHEMA.requiresSchemaCreation()).isTrue();
        assertThat(IsolationStrategy.POSTGRESQL_SCHEMA.requiresDedicatedDatasource()).isFalse();

        assertThat(IsolationStrategy.DEDICATED_DB.getTierLevel()).isEqualTo(3);
        assertThat(IsolationStrategy.DEDICATED_DB.requiresSchemaCreation()).isFalse();
        assertThat(IsolationStrategy.DEDICATED_DB.requiresDedicatedDatasource()).isTrue();

        assertThat(IsolationStrategy.fromCode("postgresql_schema")).isEqualTo(IsolationStrategy.POSTGRESQL_SCHEMA);
        assertThat(IsolationStrategy.fromCode("shared_db")).isEqualTo(IsolationStrategy.SHARED_DB);
        assertThat(IsolationStrategy.fromCode(null)).isEqualTo(IsolationStrategy.SHARED_DB);

        assertThatThrownBy(() -> IsolationStrategy.fromCode("UNKNOWN_STRATEGY"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("CircuitState: verify execution permission and tripped states")
    void testCircuitState() {
        assertThat(CircuitState.CLOSED.allowsExecution()).isTrue();
        assertThat(CircuitState.CLOSED.isTripped()).isFalse();

        assertThat(CircuitState.OPEN.allowsExecution()).isFalse();
        assertThat(CircuitState.OPEN.isTripped()).isTrue();

        assertThat(CircuitState.HALF_OPEN.allowsExecution()).isTrue();
        assertThat(CircuitState.HALF_OPEN.isTripped()).isFalse();

        assertThat(CircuitState.FORCED_OPEN.allowsExecution()).isFalse();
        assertThat(CircuitState.FORCED_OPEN.isTripped()).isTrue();
    }

    @Test
    @DisplayName("SmsProviderType: verify provider configurations")
    void testSmsProviderType() {
        assertThat(SmsProviderType.KAVENEGAR.getDisplayName()).isEqualTo("Kavenegar");
        assertThat(SmsProviderType.KAVENEGAR.isSupportsInternational()).isFalse();

        assertThat(SmsProviderType.TWILIO.isSupportsInternational()).isTrue();
        assertThat(SmsProviderType.MOCK.isSupportsInternational()).isTrue();
    }

    @Test
    @DisplayName("UserId: verify validation, equality, and auto-generation")
    void testUserIdValueObject() {
        UserId generated = UserId.generate();
        assertThat(generated.getValue()).isNotBlank();

        UserId id1 = UserId.of("usr_100");
        UserId id2 = UserId.of("usr_100");
        UserId id3 = UserId.of("usr_200");

        assertThat(id1).isEqualTo(id2);
        assertThat(id1).isNotEqualTo(id3);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        assertThat(id1.toString()).isEqualTo("usr_100");

        assertThatThrownBy(() -> new UserId(null))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new UserId("   "))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("TenantId: verify formatting rules and default tenant")
    void testTenantIdValueObject() {
        TenantId tenantId = TenantId.of("Tenant_Corp-A");
        assertThat(tenantId.getValue()).isEqualTo("tenant_corp-a");
        assertThat(tenantId.isDefault()).isFalse();

        TenantId defaultTenant = TenantId.defaultTenant();
        assertThat(defaultTenant.getValue()).isEqualTo("default");
        assertThat(defaultTenant.isDefault()).isTrue();

        assertThatThrownBy(() -> new TenantId(null))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new TenantId("x")) // too short (<2 chars)
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new TenantId("invalid tenant!@#"))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("PhoneNumber: verify Iranian normalization and international formats")
    void testPhoneNumberValueObject() {
        // Iranian zero-prefix format
        PhoneNumber phone1 = PhoneNumber.of("09121234567");
        assertThat(phone1.getValue()).isEqualTo("+989121234567");
        assertThat(phone1.isIranian()).isTrue();

        // Iranian without zero prefix
        PhoneNumber phone2 = PhoneNumber.of("9121234567");
        assertThat(phone2.getValue()).isEqualTo("+989121234567");

        // Iranian with spaces or hyphens
        PhoneNumber phone3 = PhoneNumber.of("0912-123 4567");
        assertThat(phone3.getValue()).isEqualTo("+989121234567");

        // International format
        PhoneNumber international = PhoneNumber.of("+14155552671");
        assertThat(international.getValue()).isEqualTo("+14155552671");
        assertThat(international.isIranian()).isFalse();

        // Invalid formats
        assertThatThrownBy(() -> new PhoneNumber(null))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new PhoneNumber("12345"))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new PhoneNumber("abc-phone"))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("EmailAddress: verify RFC compliance and domain extraction")
    void testEmailAddressValueObject() {
        EmailAddress email = EmailAddress.of("ADMIN@Company.com");
        assertThat(email.getValue()).isEqualTo("admin@company.com");
        assertThat(email.getDomain()).isEqualTo("company.com");

        EmailAddress email2 = EmailAddress.of("user.test+filter@sub.domain.org");
        assertThat(email2.getDomain()).isEqualTo("sub.domain.org");

        assertThatThrownBy(() -> new EmailAddress(null))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new EmailAddress("plainaddress"))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new EmailAddress("@missinguser.com"))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("TraceId: verify auto-generation and format")
    void testTraceIdValueObject() {
        TraceId traceId = TraceId.generate();
        assertThat(traceId.getValue()).startsWith("tr-");
        assertThat(traceId.getValue().length()).isGreaterThan(10);

        TraceId explicit = TraceId.of("custom-trace-123");
        assertThat(explicit.getValue()).isEqualTo("custom-trace-123");

        assertThatThrownBy(() -> new TraceId(null))
                .isInstanceOf(DomainValidationException.class);
    }
}
