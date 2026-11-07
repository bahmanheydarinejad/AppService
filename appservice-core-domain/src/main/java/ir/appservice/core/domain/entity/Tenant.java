package ir.appservice.core.domain.entity;

import ir.appservice.core.domain.enums.IsolationStrategy;
import ir.appservice.core.domain.enums.Status;
import ir.appservice.core.domain.exception.DomainValidationException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Pure POJO Domain Entity: Tenant aggregate root.
 * Enforces Hybrid Multi-Tenant isolation rules, user/storage quotas, and kill-switch governance.
 * Zero Spring or JPA annotations. Standard JDK 17 only.
 */
public class Tenant implements Serializable {

    private String id;
    private String tenantCode;
    private String name;
    private IsolationStrategy isolationStrategy;
    private String schemaName;
    private String dbUrl;
    private Status status;
    private long quotaUsers;
    private long quotaStorageMb;
    private boolean killSwitchActive;
    private Instant createdAt;
    private Instant updatedAt;

    public Tenant() {
        this.isolationStrategy = IsolationStrategy.SHARED_DB;
        this.status = Status.ACTIVE;
        this.quotaUsers = 100L;
        this.quotaStorageMb = 1024L;
        this.killSwitchActive = false;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Tenant(String id, String tenantCode, String name, IsolationStrategy isolationStrategy) {
        if (tenantCode == null || tenantCode.trim().isEmpty()) {
            throw new DomainValidationException("tenantCode", "Tenant code cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new DomainValidationException("name", "Tenant name cannot be null or empty");
        }
        this.id = id;
        this.tenantCode = tenantCode.trim().toLowerCase();
        this.name = name.trim();
        this.isolationStrategy = isolationStrategy != null ? isolationStrategy : IsolationStrategy.SHARED_DB;
        this.status = Status.ACTIVE;
        this.quotaUsers = 100L;
        this.quotaStorageMb = 1024L;
        this.killSwitchActive = false;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean canAddUsers(long currentUserCount) {
        if (killSwitchActive || status != Status.ACTIVE) {
            return false;
        }
        return currentUserCount < quotaUsers;
    }

    public boolean canStoreMb(long currentStorageMb) {
        if (killSwitchActive || status != Status.ACTIVE) {
            return false;
        }
        return currentStorageMb < quotaStorageMb;
    }

    public void triggerKillSwitch() {
        this.killSwitchActive = true;
        this.updatedAt = Instant.now();
    }

    public void resetKillSwitch() {
        this.killSwitchActive = false;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        setStatus(Status.ACTIVE);
    }

    public void suspend() {
        setStatus(Status.SUSPENDED);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        if (tenantCode == null || tenantCode.trim().isEmpty()) {
            throw new DomainValidationException("tenantCode", "Tenant code cannot be null or empty");
        }
        this.tenantCode = tenantCode.trim().toLowerCase();
        this.updatedAt = Instant.now();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new DomainValidationException("name", "Tenant name cannot be null or empty");
        }
        this.name = name.trim();
        this.updatedAt = Instant.now();
    }

    public IsolationStrategy getIsolationStrategy() {
        return isolationStrategy;
    }

    public void setIsolationStrategy(IsolationStrategy isolationStrategy) {
        this.isolationStrategy = isolationStrategy != null ? isolationStrategy : IsolationStrategy.SHARED_DB;
        this.updatedAt = Instant.now();
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        this.updatedAt = Instant.now();
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
        this.updatedAt = Instant.now();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status != null && this.status != null && !this.status.canTransitionTo(status)) {
            throw new DomainValidationException("status",
                    String.format("Invalid tenant status transition from %s to %s", this.status, status));
        }
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public long getQuotaUsers() {
        return quotaUsers;
    }

    public void setQuotaUsers(long quotaUsers) {
        this.quotaUsers = quotaUsers;
        this.updatedAt = Instant.now();
    }

    public long getQuotaStorageMb() {
        return quotaStorageMb;
    }

    public void setQuotaStorageMb(long quotaStorageMb) {
        this.quotaStorageMb = quotaStorageMb;
        this.updatedAt = Instant.now();
    }

    public boolean isKillSwitchActive() {
        return killSwitchActive;
    }

    public void setKillSwitchActive(boolean killSwitchActive) {
        this.killSwitchActive = killSwitchActive;
        this.updatedAt = Instant.now();
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
        Tenant tenant = (Tenant) o;
        return Objects.equals(tenantCode, tenant.tenantCode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tenantCode);
    }

    @Override
    public String toString() {
        return "Tenant{" +
                "id='" + id + '\'' +
                ", tenantCode='" + tenantCode + '\'' +
                ", name='" + name + '\'' +
                ", isolationStrategy=" + isolationStrategy +
                ", status=" + status +
                ", killSwitchActive=" + killSwitchActive +
                '}';
    }
}
