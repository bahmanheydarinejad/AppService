package ir.appservice.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestPersistenceConfig.class)
@DisplayName("Liquibase Schema Migration Integration Tests")
class LiquibaseMigrationIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Verify master Liquibase changelogs execute cleanly and generate all core tables")
    void testLiquibaseTablesCreation() throws Exception {
        Set<String> tableNames = new HashSet<>();

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    tableNames.add(tables.getString("TABLE_NAME").toLowerCase());
                }
            }
        }

        // Verify all 13 changelog target tables exist in the migrated schema
        assertThat(tableNames).contains(
                "document",
                "natural_person",
                "account",
                "role",
                "menu",
                "panel",
                "email",
                "reset_password_token",
                "role_accounts",
                "role_panels",
                "email_attachments",
                "tenant_registry",
                "audit_event",
                "sms_provider_config",
                "sms_log",
                "i18n_message",
                "ai_agent_account",
                "ai_tool_audit"
        );
    }
}
