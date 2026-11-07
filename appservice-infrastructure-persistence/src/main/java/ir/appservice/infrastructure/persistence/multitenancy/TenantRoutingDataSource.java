package ir.appservice.infrastructure.persistence.multitenancy;

import ir.appservice.common.context.TenantContextHolder;
import ir.appservice.core.domain.enums.IsolationStrategy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dynamic Multi-Tenant Routing DataSource.
 * Dispatches database operations based on the active TenantContextHolder according to the Hybrid Isolation Strategy:
 * 1. Tier 1 (SHARED_DB): Routes to shared default database (tenant discriminator column filters queries).
 * 2. Tier 2 (POSTGRESQL_SCHEMA): Routes to default database and switches active connection schema to tenant's schema.
 * 3. Tier 3 (DEDICATED_DB): Routes to a dedicated tenant-specific DataSource connection pool.
 */
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private final Map<Object, Object> targetDataSourcesMap = new ConcurrentHashMap<>();
    private final Map<String, TenantIsolationMetadata> tenantMetadataMap = new ConcurrentHashMap<>();

    public TenantRoutingDataSource(DataSource defaultDataSource) {
        setDefaultTargetDataSource(defaultDataSource);
        targetDataSourcesMap.put("default", defaultDataSource);
        setTargetDataSources(targetDataSourcesMap);
        afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String activeTenant = TenantContextHolder.getTenantId();
        if (activeTenant != null && targetDataSourcesMap.containsKey(activeTenant)) {
            return activeTenant;
        }
        return "default";
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        applySchemaIsolationIfRequired(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = super.getConnection(username, password);
        applySchemaIsolationIfRequired(connection);
        return connection;
    }

    private void applySchemaIsolationIfRequired(Connection connection) throws SQLException {
        String activeTenant = TenantContextHolder.getTenantId();
        if (activeTenant != null) {
            TenantIsolationMetadata metadata = tenantMetadataMap.get(activeTenant.toLowerCase());
            if (metadata != null && metadata.strategy() == IsolationStrategy.POSTGRESQL_SCHEMA
                    && metadata.schemaName() != null && !metadata.schemaName().isBlank()) {
                String schema = metadata.schemaName().trim();
                try {
                    connection.setSchema(schema);
                } catch (SQLException e) {
                    connection.setSchema(schema.toUpperCase());
                }
            }
        }
    }

    public synchronized void registerDedicatedTenant(String tenantId, DataSource dataSource) {
        if (tenantId != null && dataSource != null) {
            String key = tenantId.trim().toLowerCase();
            targetDataSourcesMap.put(key, dataSource);
            tenantMetadataMap.put(key, new TenantIsolationMetadata(key, IsolationStrategy.DEDICATED_DB, null));
            setTargetDataSources(targetDataSourcesMap);
            afterPropertiesSet();
        }
    }

    public void registerSchemaTenant(String tenantId, String schemaName) {
        if (tenantId != null && schemaName != null) {
            String key = tenantId.trim().toLowerCase();
            tenantMetadataMap.put(key, new TenantIsolationMetadata(key, IsolationStrategy.POSTGRESQL_SCHEMA, schemaName.trim()));
        }
    }

    public Map<Object, Object> getRegisteredDataSources() {
        return Collections.unmodifiableMap(targetDataSourcesMap);
    }

    public Map<String, TenantIsolationMetadata> getTenantMetadataMap() {
        return Collections.unmodifiableMap(tenantMetadataMap);
    }

    public record TenantIsolationMetadata(String tenantId, IsolationStrategy strategy, String schemaName) {}
}
