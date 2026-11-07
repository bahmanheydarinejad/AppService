package ir.appservice.core.domain;

import ir.appservice.core.domain.entity.AuditEvent;
import ir.appservice.core.domain.entity.SmsMessage;
import ir.appservice.core.domain.entity.Tenant;
import ir.appservice.core.domain.entity.User;
import ir.appservice.core.domain.enums.CircuitState;
import ir.appservice.core.domain.enums.IsolationStrategy;
import ir.appservice.core.domain.enums.Outcome;
import ir.appservice.core.domain.enums.SmsProviderType;
import ir.appservice.core.domain.port.inbound.AuthenticateUserUseCase;
import ir.appservice.core.domain.port.inbound.AuthenticateUserUseCase.AuthenticateCommand;
import ir.appservice.core.domain.port.inbound.AuthenticateUserUseCase.AuthenticationResult;
import ir.appservice.core.domain.port.inbound.SearchEntityUseCase;
import ir.appservice.core.domain.port.inbound.SearchEntityUseCase.SearchQuery;
import ir.appservice.core.domain.port.inbound.SearchEntityUseCase.SearchResult;
import ir.appservice.core.domain.port.outbound.AuditLogPort;
import ir.appservice.core.domain.port.outbound.SmsGatewayPort;
import ir.appservice.core.domain.port.outbound.SmsGatewayPort.SmsReceipt;
import ir.appservice.core.domain.port.outbound.TenantRepositoryPort;
import ir.appservice.core.domain.port.outbound.UserRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Inbound & Outbound Ports Contract Adherence Tests")
class PortsContractAdherenceTest {

    @Test
    @DisplayName("AuthenticateUserUseCase: verify command invariants and result structures")
    void testAuthenticateUserUseCaseContract() {
        AuthenticateCommand cmd = new AuthenticateCommand("admin", "secret123", "tenant_a", "127.0.0.1", "Mozilla/5.0");
        assertThat(cmd.username()).isEqualTo("admin");
        assertThat(cmd.tenantId()).isEqualTo("tenant_a");

        User user = new User("u1", "admin", "hash", "admin@appservice.ir", "tenant_a");
        AuthenticationResult successResult = AuthenticationResult.successful(user, "jwt.token.payload");
        assertThat(successResult.success()).isTrue();
        assertThat(successResult.token()).isEqualTo("jwt.token.payload");
        assertThat(successResult.outcome()).isEqualTo(Outcome.SUCCESS);

        AuthenticationResult failedResult = AuthenticationResult.failed("Bad credentials", Outcome.FAILURE);
        assertThat(failedResult.success()).isFalse();
        assertThat(failedResult.errorMessage()).isEqualTo("Bad credentials");

        assertThatThrownBy(() -> new AuthenticateCommand(null, "pwd", "tenant", "ip", "agent"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AuthenticateCommand("user", "", "tenant", "ip", "agent"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SearchEntityUseCase: verify query normalization and paged result calculation")
    void testSearchEntityUseCaseContract() {
        SearchQuery query = SearchQuery.of("users", 1, 10);
        assertThat(query.page()).isEqualTo(1);
        assertThat(query.size()).isEqualTo(10);
        assertThat(query.sortField()).isEqualTo("id");
        assertThat(query.sortDirection()).isEqualTo("ASC");

        List<String> mockItems = List.of("item1", "item2", "item3");
        SearchResult<String> result = SearchResult.of(mockItems, 25L, 1, 10);
        assertThat(result.items()).hasSize(3);
        assertThat(result.totalElements()).isEqualTo(25L);
        assertThat(result.totalPages()).isEqualTo(3); // ceil(25 / 10) = 3
    }

    @Test
    @DisplayName("SmsGatewayPort: verify receipt contract factories")
    void testSmsGatewayPortReceipt() {
        SmsReceipt success = SmsReceipt.successful("msg_kavenegar_991", SmsProviderType.KAVENEGAR, 180L);
        assertThat(success.delivered()).isTrue();
        assertThat(success.providerUsed()).isEqualTo(SmsProviderType.KAVENEGAR);
        assertThat(success.latencyMs()).isEqualTo(180L);

        SmsReceipt failed = SmsReceipt.failed(SmsProviderType.MAGFA, "Gateway connection timeout");
        assertThat(failed.delivered()).isFalse();
        assertThat(failed.errorMessage()).isEqualTo("Gateway connection timeout");
    }

    @Test
    @DisplayName("Mock Outbound Ports: verify interface implementability with pure domain types")
    void testOutboundPortMockImplementation() {
        // In-memory mock UserRepositoryPort
        Map<String, User> userDb = new HashMap<>();
        UserRepositoryPort userRepo = new UserRepositoryPort() {
            @Override public Optional<User> findById(String id) { return Optional.ofNullable(userDb.get(id)); }
            @Override public Optional<User> findByUsername(String username, String tenantId) {
                return userDb.values().stream().filter(u -> u.getUsername().equals(username)).findFirst();
            }
            @Override public Optional<User> findByEmail(String email, String tenantId) {
                return userDb.values().stream().filter(u -> email.equals(u.getEmail())).findFirst();
            }
            @Override public boolean existsByUsername(String username, String tenantId) {
                return userDb.values().stream().anyMatch(u -> u.getUsername().equals(username));
            }
            @Override public User save(User user) { userDb.put(user.getId(), user); return user; }
            @Override public void deleteById(String id) { userDb.remove(id); }
            @Override public List<User> findAll(String tenantId) { return new ArrayList<>(userDb.values()); }
            @Override public long countActiveUsers(String tenantId) { return userDb.size(); }
        };

        User user = new User("usr_42", "tester", "hash", "test@test.com", "default");
        userRepo.save(user);

        assertThat(userRepo.findById("usr_42")).isPresent();
        assertThat(userRepo.existsByUsername("tester", "default")).isTrue();
        assertThat(userRepo.countActiveUsers("default")).isEqualTo(1L);

        // In-memory mock AuditLogPort
        List<AuditEvent> auditDb = new ArrayList<>();
        AuditLogPort auditRepo = new AuditLogPort() {
            @Override public void recordEvent(AuditEvent event) { auditDb.add(event); }
            @Override public List<AuditEvent> findByTraceId(String traceId) {
                return auditDb.stream().filter(a -> traceId.equals(a.getTraceId())).toList();
            }
            @Override public List<AuditEvent> findRecentEvents(int limit, String tenantId) { return auditDb; }
            @Override public List<AuditEvent> findByActor(String actor, String tenantId) {
                return auditDb.stream().filter(a -> actor.equals(a.getActor())).toList();
            }
        };

        AuditEvent event = new AuditEvent("tr-test", "admin", "AUTH_LOGIN", "User", null, Outcome.SUCCESS, "127.0.0.1", "default");
        auditRepo.recordEvent(event);
        assertThat(auditRepo.findByTraceId("tr-test")).hasSize(1);
    }
}
