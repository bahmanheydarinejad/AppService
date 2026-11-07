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
 * Pure POJO Domain Entity: OAuth2Client.
 * Represents an OAuth 2.1 / OIDC registered client application with PKCE enforcement,
 * SHA-256 hashed secret storage, and rate-limiting quotas.
 * Zero Spring or JPA annotations. Standard JDK 17 only.
 */
public class OAuth2Client implements Serializable {

    private String clientId;
    private String clientSecretHash;
    private String clientName;
    private Set<String> scopes = new HashSet<>();
    private Set<String> grantTypes = new HashSet<>();
    private Set<String> redirectUris = new HashSet<>();
    private boolean requirePkce = true;
    private int rateLimitPerMinute = 60;
    private Status status = Status.ACTIVE;
    private String tenantId;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public OAuth2Client() {
    }

    public OAuth2Client(String clientId, String clientSecretHash, String clientName, String tenantId) {
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new DomainValidationException("clientId", "OAuth2 Client ID cannot be null or empty");
        }
        if (clientSecretHash == null || clientSecretHash.trim().isEmpty()) {
            throw new DomainValidationException("clientSecretHash", "OAuth2 Client Secret Hash cannot be null or empty");
        }
        this.clientId = clientId.trim();
        this.clientSecretHash = clientSecretHash.trim();
        this.clientName = clientName != null ? clientName.trim() : this.clientId;
        this.tenantId = tenantId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecretHash() {
        return clientSecretHash;
    }

    public void setClientSecretHash(String clientSecretHash) {
        this.clientSecretHash = clientSecretHash;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
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

    public Set<String> getGrantTypes() {
        return Collections.unmodifiableSet(grantTypes);
    }

    public void setGrantTypes(Set<String> grantTypes) {
        this.grantTypes = grantTypes != null ? new HashSet<>(grantTypes) : new HashSet<>();
    }

    public void addGrantType(String grantType) {
        if (grantType != null && !grantType.trim().isEmpty()) {
            this.grantTypes.add(grantType.trim());
        }
    }

    public Set<String> getRedirectUris() {
        return Collections.unmodifiableSet(redirectUris);
    }

    public void setRedirectUris(Set<String> redirectUris) {
        this.redirectUris = redirectUris != null ? new HashSet<>(redirectUris) : new HashSet<>();
    }

    public boolean isRequirePkce() {
        return requirePkce;
    }

    public void setRequirePkce(boolean requirePkce) {
        this.requirePkce = requirePkce;
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
        OAuth2Client that = (OAuth2Client) o;
        return Objects.equals(clientId, that.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId);
    }

    @Override
    public String toString() {
        return "OAuth2Client{" +
                "clientId='" + clientId + '\'' +
                ", clientName='" + clientName + '\'' +
                ", requirePkce=" + requirePkce +
                ", status=" + status +
                '}';
    }
}
