package ir.appservice.core.domain.entity;

import ir.appservice.core.domain.enums.Status;
import ir.appservice.core.domain.exception.DomainValidationException;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Pure POJO Domain Entity: ApiKey.
 * Represents an API consumer key with SHA-256 hashed secret, rate limits, and scopes.
 * Plaintext secrets are never stored.
 * Zero Spring or JPA annotations. Standard JDK 17 only.
 */
public class ApiKey implements Serializable {

    private String id;
    private String keyId;
    private String secretHash;
    private String name;
    private Set<String> scopes = new HashSet<>();
    private int rateLimitPerMinute = 120;
    private Status status = Status.ACTIVE;
    private String tenantId;
    private Instant expiresAt;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public ApiKey() {
    }

    public ApiKey(String id, String keyId, String secretHash, String name, String tenantId) {
        if (keyId == null || keyId.trim().isEmpty()) {
            throw new DomainValidationException("keyId", "API Key ID cannot be null or empty");
        }
        if (secretHash == null || secretHash.trim().isEmpty()) {
            throw new DomainValidationException("secretHash", "API Key secret hash cannot be null or empty");
        }
        this.id = id;
        this.keyId = keyId.trim();
        this.secretHash = secretHash.trim();
        this.name = name != null ? name.trim() : this.keyId;
        this.tenantId = tenantId;
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return status == Status.ACTIVE && !isExpired();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Set<String> getScopes() {
        return Collections.unmodifiableSet(scopes);
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes != null ? new HashSet<>(scopes) : new HashSet<>();
    }

    public void addScope(String scope) {
        if (scope != null && !scope.trim().isEmpty()) {
            this.scopes.add(scope.trim());
        }
    }

    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public void setRateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = Math.max(1, rateLimitPerMinute);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status != null ? status : Status.ACTIVE;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiKey apiKey = (ApiKey) o;
        return Objects.equals(keyId, apiKey.keyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyId);
    }

    @Override
    public String toString() {
        return "ApiKey{" +
                "keyId='" + keyId + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", expired=" + isExpired() +
                '}';
    }
}
