package ir.appservice.infrastructure.security;

import ir.appservice.core.domain.entity.ApiKey;
import ir.appservice.core.domain.port.outbound.ApiKeyRepositoryPort;
import ir.appservice.infrastructure.security.apikey.ApiKeyService;
import ir.appservice.infrastructure.security.ratelimit.TokenBucketRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("API Key Service & Token Bucket Rate Limiter Tests")
class ApiKeyAndRateLimiterTest {

    private ApiKeyRepositoryPort repository;
    private TokenBucketRateLimiter rateLimiter;
    private ApiKeyService apiKeyService;
    private final Map<String, ApiKey> inMemoryDb = new HashMap<>();

    @BeforeEach
    void setUp() {
        inMemoryDb.clear();
        repository = new ApiKeyRepositoryPort() {
            @Override public Optional<ApiKey> findByKeyId(String keyId) { return Optional.ofNullable(inMemoryDb.get(keyId)); }
            @Override public Optional<ApiKey> findById(String id) {
                return inMemoryDb.values().stream().filter(k -> id.equals(k.getId())).findFirst();
            }
            @Override public boolean existsByKeyId(String keyId) { return inMemoryDb.containsKey(keyId); }
            @Override public ApiKey save(ApiKey apiKey) { inMemoryDb.put(apiKey.getKeyId(), apiKey); return apiKey; }
            @Override public void deleteById(String id) { inMemoryDb.values().removeIf(k -> id.equals(k.getId())); }
            @Override public void deleteByKeyId(String keyId) { inMemoryDb.remove(keyId); }
            @Override public List<ApiKey> findAllByTenantId(String tenantId) {
                return inMemoryDb.values().stream().filter(k -> tenantId.equals(k.getTenantId())).toList();
            }
        };

        rateLimiter = new TokenBucketRateLimiter();
        apiKeyService = new ApiKeyService(repository, rateLimiter);
    }

    @Test
    @DisplayName("API Key generation stores SHA-256 hash and never persists raw secret")
    void testApiKeyGenerationHashing() {
        ApiKeyService.ApiKeyResult result = apiKeyService.generateApiKey(
                "Payment Microservice Key",
                Set.of("PAYMENTS_READ", "PAYMENTS_WRITE"),
                60,
                "tenant_fin",
                Instant.now().plus(30, ChronoUnit.DAYS)
        );

        assertThat(result.keyId()).startsWith("ak_live_");
        assertThat(result.rawSecret()).startsWith("sec_");

        ApiKey stored = inMemoryDb.get(result.keyId());
        assertThat(stored).isNotNull();
        // Stored secret must be a 64-character SHA-256 hex string, NOT the plaintext rawSecret!
        assertThat(stored.getSecretHash()).hasSize(64);
        assertThat(stored.getSecretHash()).isNotEqualTo(result.rawSecret());
        assertThat(stored.getSecretHash()).isEqualTo(apiKeyService.hashSecret(result.rawSecret()));
    }

    @Test
    @DisplayName("API Key authentication succeeds with valid key and secret")
    void testValidApiKeyAuthentication() {
        ApiKeyService.ApiKeyResult result = apiKeyService.generateApiKey(
                "Reporting Worker",
                Set.of("REPORTS_VIEW"),
                100,
                "tenant_alpha",
                Instant.now().plus(1, ChronoUnit.HOURS)
        );

        Optional<ApiKey> authenticated = apiKeyService.authenticateApiKey(result.keyId(), result.rawSecret());
        assertThat(authenticated).isPresent();
        assertThat(authenticated.get().getName()).isEqualTo("Reporting Worker");
        assertThat(authenticated.get().getTenantId()).isEqualTo("tenant_alpha");
    }

    @Test
    @DisplayName("API Key authentication fails on invalid secret or unknown keyId")
    void testInvalidApiKeyAuthentication() {
        ApiKeyService.ApiKeyResult result = apiKeyService.generateApiKey(
                "Test Worker",
                Set.of("TEST"),
                100,
                "tenant_alpha",
                Instant.now().plus(1, ChronoUnit.HOURS)
        );

        Optional<ApiKey> wrongSecret = apiKeyService.authenticateApiKey(result.keyId(), "sec_invalidSecret12345");
        assertThat(wrongSecret).isEmpty();

        Optional<ApiKey> unknownKey = apiKeyService.authenticateApiKey("ak_live_unknownKeyId", result.rawSecret());
        assertThat(unknownKey).isEmpty();
    }

    @Test
    @DisplayName("Expired API Key is automatically rejected")
    void testExpiredApiKey() {
        ApiKeyService.ApiKeyResult result = apiKeyService.generateApiKey(
                "Expired Key",
                Set.of("TEST"),
                100,
                "tenant_alpha",
                Instant.now().minus(10, ChronoUnit.MINUTES) // Expired 10 mins ago
        );

        Optional<ApiKey> authenticated = apiKeyService.authenticateApiKey(result.keyId(), result.rawSecret());
        assertThat(authenticated).isEmpty();
    }

    @Test
    @DisplayName("Token Bucket Rate Limiter permits allowed quota and throttles exceeding requests")
    void testTokenBucketThrottling() {
        String testKey = "test_rate_bucket";
        int capacity = 5;

        // Consume 5 allowed tokens
        for (int i = 0; i < capacity; i++) {
            boolean allowed = rateLimiter.tryConsume(testKey, 1, capacity, capacity, 60_000L);
            assertThat(allowed).isTrue();
        }

        // 6th request must be throttled
        boolean throttled = rateLimiter.tryConsume(testKey, 1, capacity, capacity, 60_000L);
        assertThat(throttled).isFalse();

        // Reset clears bucket state
        rateLimiter.reset(testKey);
        assertThat(rateLimiter.tryConsume(testKey, 1, capacity, capacity, 60_000L)).isTrue();
    }
}
