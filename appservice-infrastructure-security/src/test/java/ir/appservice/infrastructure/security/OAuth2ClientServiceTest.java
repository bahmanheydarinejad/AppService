package ir.appservice.infrastructure.security;

import ir.appservice.core.domain.entity.OAuth2Client;
import ir.appservice.core.domain.port.outbound.OAuth2ClientRepositoryPort;
import ir.appservice.infrastructure.security.oauth2.OAuth2ClientService;
import ir.appservice.infrastructure.security.pkce.PkceChallengeVerifier;
import ir.appservice.infrastructure.security.ratelimit.TokenBucketRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OAuth2 Client Management Service Tests")
class OAuth2ClientServiceTest {

    private OAuth2ClientRepositoryPort repository;
    private PkceChallengeVerifier pkceVerifier;
    private TokenBucketRateLimiter rateLimiter;
    private OAuth2ClientService clientService;
    private final Map<String, OAuth2Client> inMemoryDb = new HashMap<>();

    @BeforeEach
    void setUp() {
        inMemoryDb.clear();
        repository = new OAuth2ClientRepositoryPort() {
            @Override public Optional<OAuth2Client> findByClientId(String clientId) { return Optional.ofNullable(inMemoryDb.get(clientId)); }
            @Override public boolean existsByClientId(String clientId) { return inMemoryDb.containsKey(clientId); }
            @Override public OAuth2Client save(OAuth2Client client) { inMemoryDb.put(client.getClientId(), client); return client; }
            @Override public void deleteByClientId(String clientId) { inMemoryDb.remove(clientId); }
            @Override public List<OAuth2Client> findAllByTenantId(String tenantId) {
                return inMemoryDb.values().stream().filter(c -> tenantId.equals(c.getTenantId())).toList();
            }
        };

        pkceVerifier = new PkceChallengeVerifier();
        rateLimiter = new TokenBucketRateLimiter();
        clientService = new OAuth2ClientService(repository, pkceVerifier, rateLimiter);
    }

    @Test
    @DisplayName("Register client generates SHA-256 secret hash and preserves PKCE flags")
    void testClientRegistration() {
        OAuth2ClientService.ClientRegistrationResult result = clientService.registerClient(
                "Mobile Flutter Client",
                Set.of("openid", "profile", "read"),
                Set.of("authorization_code", "refresh_token"),
                Set.of("https://app.appservice.ir/callback"),
                true,
                100,
                "tenant_corp"
        );

        assertThat(result.clientId()).startsWith("client_");
        assertThat(result.rawClientSecret()).startsWith("cs_");

        OAuth2Client saved = inMemoryDb.get(result.clientId());
        assertThat(saved).isNotNull();
        assertThat(saved.isRequirePkce()).isTrue();
        assertThat(saved.getClientSecretHash()).hasSize(64);
        assertThat(saved.getClientSecretHash()).isNotEqualTo(result.rawClientSecret());

        // Authenticate succeeds
        Optional<OAuth2Client> authenticated = clientService.authenticateClient(result.clientId(), result.rawClientSecret());
        assertThat(authenticated).isPresent();

        // Wrong secret fails
        Optional<OAuth2Client> failedAuth = clientService.authenticateClient(result.clientId(), "cs_invalidSecret");
        assertThat(failedAuth).isEmpty();
    }

    @Test
    @DisplayName("Verify PKCE enforcement on client service")
    void testPkceEnforcement() {
        OAuth2ClientService.ClientRegistrationResult result = clientService.registerClient(
                "SPA Web App",
                Set.of("read"),
                Set.of("authorization_code"),
                Set.of("http://localhost:3000/callback"),
                true,
                100,
                "tenant_corp"
        );

        String verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        String challenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";

        boolean validPkce = clientService.verifyPkce(result.clientId(), verifier, challenge, "S256");
        assertThat(validPkce).isTrue();

        boolean invalidPkce = clientService.verifyPkce(result.clientId(), "invalidVerifier123456789012345678901234567890", challenge, "S256");
        assertThat(invalidPkce).isFalse();
    }
}
