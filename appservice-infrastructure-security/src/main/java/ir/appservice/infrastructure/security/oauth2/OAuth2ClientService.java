package ir.appservice.infrastructure.security.oauth2;

import ir.appservice.core.domain.entity.OAuth2Client;
import ir.appservice.core.domain.enums.Status;
import ir.appservice.core.domain.port.outbound.OAuth2ClientRepositoryPort;
import ir.appservice.infrastructure.security.pkce.PkceChallengeVerifier;
import ir.appservice.infrastructure.security.ratelimit.TokenBucketRateLimiter;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;

/**
 * Service for OAuth2.1 / OIDC Client registration, secret hashing, PKCE verification, and rate limiting.
 */
@Service
public class OAuth2ClientService {

    private final OAuth2ClientRepositoryPort clientRepository;
    private final PkceChallengeVerifier pkceVerifier;
    private final TokenBucketRateLimiter rateLimiter;
    private final SecureRandom secureRandom = new SecureRandom();

    public OAuth2ClientService(OAuth2ClientRepositoryPort clientRepository,
                               PkceChallengeVerifier pkceVerifier,
                               TokenBucketRateLimiter rateLimiter) {
        this.clientRepository = clientRepository;
        this.pkceVerifier = pkceVerifier;
        this.rateLimiter = rateLimiter;
    }

    public record ClientRegistrationResult(String clientId, String rawClientSecret, OAuth2Client client) {}

    /**
     * Registers a new OAuth2 client.
     * The raw client secret is returned once and only its SHA-256 hash is persisted.
     */
    public ClientRegistrationResult registerClient(String clientName,
                                                  Set<String> scopes,
                                                  Set<String> grantTypes,
                                                  Set<String> redirectUris,
                                                  boolean requirePkce,
                                                  int rateLimitPerMinute,
                                                  String tenantId) {
        byte[] idBytes = new byte[12];
        byte[] secretBytes = new byte[32];
        secureRandom.nextBytes(idBytes);
        secureRandom.nextBytes(secretBytes);

        String clientId = "client_" + HexFormat.of().formatHex(idBytes);
        String rawSecret = "cs_" + Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);
        String secretHash = hashSecret(rawSecret);

        OAuth2Client client = new OAuth2Client(clientId, secretHash, clientName, tenantId);
        client.setScopes(scopes);
        client.setGrantTypes(grantTypes);
        client.setRedirectUris(redirectUris);
        client.setRequirePkce(requirePkce);
        client.setRateLimitPerMinute(rateLimitPerMinute > 0 ? rateLimitPerMinute : 60);
        client.setStatus(Status.ACTIVE);

        OAuth2Client saved = clientRepository.save(client);
        return new ClientRegistrationResult(clientId, rawSecret, saved);
    }

    /**
     * Validates client credentials and checks rate limits.
     */
    public Optional<OAuth2Client> authenticateClient(String clientId, String rawClientSecret) {
        if (clientId == null || rawClientSecret == null) {
            return Optional.empty();
        }

        Optional<OAuth2Client> clientOpt = clientRepository.findByClientId(clientId);
        if (clientOpt.isEmpty()) {
            return Optional.empty();
        }

        OAuth2Client client = clientOpt.get();
        if (client.getStatus() != Status.ACTIVE) {
            return Optional.empty();
        }

        String computedHash = hashSecret(rawClientSecret);
        if (!timingSafeEquals(computedHash, client.getClientSecretHash())) {
            return Optional.empty();
        }

        // Check rate limiter
        boolean allowed = rateLimiter.tryConsumePerMinute(client.getClientId(), client.getRateLimitPerMinute());
        if (!allowed) {
            return Optional.empty();
        }

        return Optional.of(client);
    }

    /**
     * Verifies PKCE code challenge and verifier according to OAuth 2.1 RFC 7636.
     */
    public boolean verifyPkce(String clientId, String codeVerifier, String codeChallenge, String method) {
        Optional<OAuth2Client> clientOpt = clientRepository.findByClientId(clientId);
        if (clientOpt.isEmpty()) {
            return false;
        }

        OAuth2Client client = clientOpt.get();
        if (client.isRequirePkce()) {
            // OAuth 2.1 mandates S256 for public clients with PKCE
            if (codeVerifier == null || codeChallenge == null) {
                return false;
            }
            return pkceVerifier.verify(codeVerifier, codeChallenge, method != null ? method : PkceChallengeVerifier.METHOD_S256);
        }

        return true;
    }

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
