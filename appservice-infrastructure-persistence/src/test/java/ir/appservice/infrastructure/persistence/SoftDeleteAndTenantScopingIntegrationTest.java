package ir.appservice.infrastructure.persistence;

import ir.appservice.core.domain.entity.AuditEvent;
import ir.appservice.core.domain.entity.Tenant;
import ir.appservice.core.domain.entity.User;
import ir.appservice.core.domain.enums.IsolationStrategy;
import ir.appservice.core.domain.enums.Outcome;
import ir.appservice.infrastructure.persistence.adapter.AuditLogAdapter;
import ir.appservice.infrastructure.persistence.adapter.TenantRepositoryAdapter;
import ir.appservice.infrastructure.persistence.adapter.UserRepositoryAdapter;
import ir.appservice.infrastructure.persistence.repository.SpringDataAuditLogRepository;
import ir.appservice.infrastructure.persistence.repository.SpringDataTenantRepository;
import ir.appservice.infrastructure.persistence.repository.SpringDataUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestPersistenceConfig.class)
@DisplayName("Soft-Deletion and Multi-Tenant Scoping Integration Tests")
class SoftDeleteAndTenantScopingIntegrationTest {

    @Autowired
    private UserRepositoryAdapter userRepositoryAdapter;

    @Autowired
    private TenantRepositoryAdapter tenantRepositoryAdapter;

    @Autowired
    private AuditLogAdapter auditLogAdapter;

    @Autowired
    private SpringDataUserRepository springDataUserRepository;

    @Autowired
    private SpringDataTenantRepository springDataTenantRepository;

    @Autowired
    private SpringDataAuditLogRepository springDataAuditLogRepository;

    @BeforeEach
    void setUp() {
        springDataUserRepository.deleteAll();
        springDataTenantRepository.deleteAll();
        springDataAuditLogRepository.deleteAll();
    }

    @Test
    @DisplayName("User: verify soft-deletion automatically excludes record from active searches")
    void testUserSoftDeletion() {
        User user = new User("usr_10", "john_doe", "hashed_pwd", "john@example.com", "tenant_a");
        user.setFirstName("John");
        user.setLastName("Doe");
        userRepositoryAdapter.save(user);

        assertThat(userRepositoryAdapter.findById("usr_10")).isPresent();
        assertThat(userRepositoryAdapter.existsByUsername("john_doe", "tenant_a")).isTrue();

        // Perform soft delete
        userRepositoryAdapter.deleteById("usr_10");

        // Verify soft-deleted entity is invisible to findById, existsByUsername, and findAll
        assertThat(userRepositoryAdapter.findById("usr_10")).isEmpty();
        assertThat(userRepositoryAdapter.existsByUsername("john_doe", "tenant_a")).isFalse();
        assertThat(userRepositoryAdapter.findAll("tenant_a")).isEmpty();

        // Verify entity physically still exists in database with populated delete_date
        assertThat(springDataUserRepository.findById("usr_10")).isPresent();
        assertThat(springDataUserRepository.findById("usr_10").get().getDeleteDate()).isNotNull();
    }

    @Test
    @DisplayName("User: verify tenant isolation prevents cross-tenant username resolution")
    void testTenantScopingIsolation() {
        User userTenantA = new User("usr_a", "finance_manager", "hash1", "a@corp.com", "tenant_a");
        User userTenantB = new User("usr_b", "finance_manager", "hash2", "b@corp.com", "tenant_b");

        userRepositoryAdapter.save(userTenantA);
        userRepositoryAdapter.save(userTenantB);

        Optional<User> foundA = userRepositoryAdapter.findByUsername("finance_manager", "tenant_a");
        assertThat(foundA).isPresent();
        assertThat(foundA.get().getId()).isEqualTo("usr_a");

        Optional<User> foundB = userRepositoryAdapter.findByUsername("finance_manager", "tenant_b");
        assertThat(foundB).isPresent();
        assertThat(foundB.get().getId()).isEqualTo("usr_b");

        // Querying with mismatched tenant returns empty
        assertThat(userRepositoryAdapter.findByUsername("finance_manager", "tenant_c")).isEmpty();
    }

    @Test
    @DisplayName("Tenant & Audit: verify persistence and retrieval via Outbound Ports")
    void testTenantAndAuditPersistence() {
        Tenant tenant = new Tenant("tnt_alpha", "alpha_org", "Alpha Corp", IsolationStrategy.POSTGRESQL_SCHEMA);
        tenantRepositoryAdapter.save(tenant);

        assertThat(tenantRepositoryAdapter.findByTenantCode("alpha_org")).isPresent();

        AuditEvent event = new AuditEvent(
                "tr-audit-test-99",
                "admin",
                "UPDATE_PASSWORD",
                "Account:usr_10",
                "{\"status\": \"CHANGED\"}",
                Outcome.SUCCESS,
                "127.0.0.1",
                "alpha_org"
        );
        auditLogAdapter.recordEvent(event);

        List<AuditEvent> auditEvents = auditLogAdapter.findByTraceId("tr-audit-test-99");
        assertThat(auditEvents).hasSize(1);
        assertThat(auditEvents.get(0).getAction()).isEqualTo("UPDATE_PASSWORD");
    }
}
