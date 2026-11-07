package ir.appservice.infrastructure.persistence;

import ir.appservice.common.context.TenantContextHolder;
import ir.appservice.core.domain.enums.IsolationStrategy;
import ir.appservice.infrastructure.persistence.multitenancy.TenantRoutingDataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TenantRoutingDataSource Dynamic Hybrid Isolation Tests")
class TenantRoutingDataSourceTest {

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("Verify routing dispatches between default, dedicated DB, and schema isolation")
    void testDynamicDataSourceRouting() throws SQLException {
        JdbcDataSource defaultDs = new JdbcDataSource();
        defaultDs.setURL("jdbc:h2:mem:default_db;DB_CLOSE_DELAY=-1");

        TenantRoutingDataSource routingDs = new TenantRoutingDataSource(defaultDs);

        // 1. Unbound tenant -> routes to default datasource
        TenantContextHolder.clear();
        try (Connection conn = routingDs.getConnection()) {
            assertThat(conn).isNotNull();
        }

        // 2. Register and route to dedicated tenant datasource
        JdbcDataSource tenantAlphaDs = new JdbcDataSource();
        tenantAlphaDs.setURL("jdbc:h2:mem:tenant_alpha_db;DB_CLOSE_DELAY=-1");
        routingDs.registerDedicatedTenant("tenant_alpha", tenantAlphaDs);

        assertThat(routingDs.getRegisteredDataSources()).containsKey("tenant_alpha");

        TenantContextHolder.setTenantId("tenant_alpha");
        try (Connection conn = routingDs.getConnection()) {
            assertThat(conn).isNotNull();
        }

        // 3. Register schema-isolated tenant and verify schema setting
        try (Connection conn = defaultDs.getConnection()) {
            conn.createStatement().execute("CREATE SCHEMA IF NOT EXISTS schema_beta");
        }

        routingDs.registerSchemaTenant("tenant_beta", "schema_beta");
        assertThat(routingDs.getTenantMetadataMap()).containsKey("tenant_beta");
        assertThat(routingDs.getTenantMetadataMap().get("tenant_beta").strategy())
                .isEqualTo(IsolationStrategy.POSTGRESQL_SCHEMA);

        TenantContextHolder.setTenantId("tenant_beta");
        try (Connection conn = routingDs.getConnection()) {
            assertThat(conn).isNotNull();
        }
    }
}
