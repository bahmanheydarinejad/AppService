package ir.appservice.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JPA Entity: OAuth2ClientJpaEntity mapping to oauth2_client table.
 */
@Entity
@Table(name = "oauth2_client")
public class OAuth2ClientJpaEntity implements Serializable {

    @Id
    @Column(name = "client_id", nullable = false, length = 255)
    private String clientId;

    @Column(name = "client_secret_hash", nullable = false, length = 255)
    private String clientSecretHash;

    @Column(name = "client_name", nullable = false, length = 255)
    private String clientName;

    @Column(name = "scopes", length = 1024)
    private String scopes;

    @Column(name = "grant_types", length = 512)
    private String grantTypes;

    @Column(name = "redirect_uris", length = 2048)
    private String redirectUris;

    @Column(name = "require_pkce", nullable = false)
    private boolean requirePkce = true;

    @Column(name = "rate_limit_per_minute", nullable = false)
    private int rateLimitPerMinute = 60;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "ACTIVE";

    @Column(name = "tenant_id", length = 255)
    private String tenantId;

    @Column(name = "registered_date")
    private Instant registeredDate;

    @Column(name = "updated_date")
    private Instant updatedDate;

    @Column(name = "delete_date")
    private Instant deleteDate;

    public OAuth2ClientJpaEntity() {
    }

    public OAuth2ClientJpaEntity(String clientId, String clientSecretHash, String clientName, String tenantId) {
        this.clientId = clientId;
        this.clientSecretHash = clientSecretHash;
        this.clientName = clientName;
        this.tenantId = tenantId;
    }

    @PrePersist
    protected void onPrePersist() {
        Instant now = Instant.now();
        if (this.registeredDate == null) {
            this.registeredDate = now;
        }
        this.updatedDate = now;
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.updatedDate = Instant.now();
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

    public Set<String> getGrantTypesAsSet() {
        if (grantTypes == null || grantTypes.isBlank()) return new HashSet<>();
        return Arrays.stream(grantTypes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public void setGrantTypesFromSet(Set<String> grantTypeSet) {
        if (grantTypeSet == null || grantTypeSet.isEmpty()) {
            this.grantTypes = "";
        } else {
            this.grantTypes = String.join(",", grantTypeSet);
        }
    }

    public Set<String> getRedirectUrisAsSet() {
        if (redirectUris == null || redirectUris.isBlank()) return new HashSet<>();
        return Arrays.stream(redirectUris.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public void setRedirectUrisFromSet(Set<String> redirectUriSet) {
        if (redirectUriSet == null || redirectUriSet.isEmpty()) {
            this.redirectUris = "";
        } else {
            this.redirectUris = String.join(",", redirectUriSet);
        }
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

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public String getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(String grantTypes) {
        this.grantTypes = grantTypes;
    }

    public String getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(String redirectUris) {
        this.redirectUris = redirectUris;
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
        this.rateLimitPerMinute = rateLimitPerMinute;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Instant getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(Instant registeredDate) {
        this.registeredDate = registeredDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Instant getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Instant deleteDate) {
        this.deleteDate = deleteDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuth2ClientJpaEntity that = (OAuth2ClientJpaEntity) o;
        return Objects.equals(clientId, that.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId);
    }
}
