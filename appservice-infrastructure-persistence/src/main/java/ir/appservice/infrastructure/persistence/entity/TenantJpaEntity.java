package ir.appservice.infrastructure.persistence.entity;

import ir.appservice.core.domain.enums.IsolationStrategy;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * JPA Entity mapping to `tenant_registry`.
 */
@Entity
@Table(name = "tenant_registry")
@AttributeOverride(name = "status", column = @Column(name = "status", length = 50))
public class TenantJpaEntity extends BaseJpaEntity {

    @Column(name = "tenant_code", nullable = false, unique = true, length = 64)
    private String tenantCode;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "isolation_strategy", nullable = false, length = 50)
    private IsolationStrategy isolationStrategy = IsolationStrategy.SHARED_DB;

    @Column(name = "datasource_url", length = 512)
    private String datasourceUrl;

    @Column(name = "schema_name", length = 128)
    private String schemaName;

    @Column(name = "quota_users", nullable = false)
    private long quotaUsers = 100L;

    @Column(name = "quota_storage_mb", nullable = false)
    private long quotaStorageMb = 1024L;

    @Column(name = "kill_switch_active", nullable = false)
    private boolean killSwitchActive = false;

    public TenantJpaEntity() {
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IsolationStrategy getIsolationStrategy() {
        return isolationStrategy;
    }

    public void setIsolationStrategy(IsolationStrategy isolationStrategy) {
        this.isolationStrategy = isolationStrategy;
    }

    public String getDatasourceUrl() {
        return datasourceUrl;
    }

    public void setDatasourceUrl(String datasourceUrl) {
        this.datasourceUrl = datasourceUrl;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public long getQuotaUsers() {
        return quotaUsers;
    }

    public void setQuotaUsers(long quotaUsers) {
        this.quotaUsers = quotaUsers;
    }

    public long getQuotaStorageMb() {
        return quotaStorageMb;
    }

    public void setQuotaStorageMb(long quotaStorageMb) {
        this.quotaStorageMb = quotaStorageMb;
    }

    public boolean isKillSwitchActive() {
        return killSwitchActive;
    }

    public void setKillSwitchActive(boolean killSwitchActive) {
        this.killSwitchActive = killSwitchActive;
    }
}
