package ir.appservice.infrastructure.persistence.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JPA Entity: ApiKeyJpaEntity mapping to api_key table.
 */
@Entity
@Table(name = "api_key")
@AttributeOverride(name = "status", column = @Column(name = "status", length = 50))
public class ApiKeyJpaEntity extends BaseJpaEntity {

    @Column(name = "key_id", nullable = false, unique = true, length = 128)
    private String keyId;

    @Column(name = "secret_hash", nullable = false, length = 255)
    private String secretHash;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "scopes", length = 1024)
    private String scopes;

    @Column(name = "rate_limit_per_minute", nullable = false)
    private int rateLimitPerMinute = 120;

    @Column(name = "tenant_id", length = 255)
    private String tenantId;

    @Column(name = "expires_at")
    private Instant expiresAt;

    public ApiKeyJpaEntity() {
    }

    public ApiKeyJpaEntity(String keyId, String secretHash, String name, String tenantId) {
        this.keyId = keyId;
        this.secretHash = secretHash;
        this.name = name;
        this.tenantId = tenantId;
    }

    public Set<String> getScopesAsSet() {
        if (scopes == null || scopes.isBlank()) return new HashSet<>();
        return Arrays.stream(scopes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public void setScopesFromSet(Set<String> scopeSet) {
        if (scopeSet == null || scopeSet.isEmpty()) {
            this.scopes = "";
        } else {
            this.scopes = String.join(",", scopeSet);
        }
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getSecretHash() {
        return secretHash;
    }

    public void setSecretHash(String secretHash) {
        this.secretHash = secretHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public void setRateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
