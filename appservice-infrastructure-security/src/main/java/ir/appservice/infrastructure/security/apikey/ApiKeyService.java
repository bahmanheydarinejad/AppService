package ir.appservice.infrastructure.security.apikey;

import ir.appservice.core.domain.entity.ApiKey;
import ir.appservice.core.domain.enums.Status;
import ir.appservice.core.domain.port.outbound.ApiKeyRepositoryPort;
import ir.appservice.infrastructure.security.ratelimit.TokenBucketRateLimiter;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service for API Key generation, verification, SHA-256 secret hashing, and rate limiting.
 * Plaintext secrets are never stored and cannot be retrieved after generation.
 */
@Service
public class ApiKeyService {

    private final ApiKeyRepositoryPort apiKeyRepository;
    private final TokenBucketRateLimiter rateLimiter;
    private final SecureRandom secureRandom = new SecureRandom();

    public ApiKeyService(ApiKeyRepositoryPort apiKeyRepository, TokenBucketRateLimiter rateLimiter) {
        this.apiKeyRepository = apiKeyRepository;
        this.rateLimiter = rateLimiter;
    }

    public record ApiKeyResult(String keyId, String rawSecret, ApiKey apiKey) {}

    /**
     * Generates a new cryptographically secure API Key pair.
     * Plaintext rawSecret is returned once and only its SHA-256 hash is saved.
     */
    public ApiKeyResult generateApiKey(String name, Set<String> scopes, int rateLimitPerMinute, String tenantId, Instant expiresAt) {
        byte[] keyBytes = new byte[16];
        byte[] secretBytes = new byte[32];
        secureRandom.nextBytes(keyBytes);
        secureRandom.nextBytes(secretBytes);

        String keyId = "ak_live_" + HexFormat.of().formatHex(keyBytes);
        String rawSecret = "sec_" + Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);
        String secretHash = hashSecret(rawSecret);

        ApiKey apiKey = new ApiKey(UUID.randomUUID().toString(), keyId, secretHash, name, tenantId);
        apiKey.setScopes(scopes);
        apiKey.setRateLimitPerMinute(rateLimitPerMinute > 0 ? rateLimitPerMinute : 120);
        apiKey.setStatus(Status.ACTIVE);
        apiKey.setExpiresAt(expiresAt);

        ApiKey saved = apiKeyRepository.save(apiKey);
        return new ApiKeyResult(keyId, rawSecret, saved);
    }

    /**
     * Validates API Key credentials and verifies rate limits.
     *
     * @param keyId     Public API key identifier
     * @param rawSecret Provided plaintext secret
     * @return Optional containing ApiKey if valid and not throttled; empty otherwise
     */
    public Optional<ApiKey> authenticateApiKey(String keyId, String rawSecret) {
        if (keyId == null || rawSecret == null) {
            return Optional.empty();
        }

        Optional<ApiKey> keyOpt = apiKeyRepository.findByKeyId(keyId);
        if (keyOpt.isEmpty()) {
            return Optional.empty();
        }

        ApiKey apiKey = keyOpt.get();
        if (!apiKey.isActive()) {
            return Optional.empty();
        }

        String computedHash = hashSecret(rawSecret);
        if (!timingSafeEquals(computedHash, apiKey.getSecretHash())) {
            return Optional.empty();
        }

        // Enforce Token Bucket rate limiting
        boolean allowed = rateLimiter.tryConsumePerMinute(apiKey.getKeyId(), apiKey.getRateLimitPerMinute());
        if (!allowed) {
            return Optional.empty(); // Throttled
        }

        return Optional.of(apiKey);
    }

    /**
     * Computes SHA-256 hash formatted as a hex string.
     */
    public String hashSecret(String rawSecret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawSecret.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private boolean timingSafeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(aBytes, bBytes);
    }
}
