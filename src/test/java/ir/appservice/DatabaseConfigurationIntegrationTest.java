package ir.appservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Run.class)
public class DatabaseConfigurationIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    @Test
    public void testH2InMemoryDatabaseConfiguration() throws SQLException {
        assertNotNull(dataSource, "DataSource must be configured");

        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Database connection should not be null");
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            String url = metaData.getURL();

            assertEquals("H2", databaseProductName, "Database product must be H2");
            assertTrue(url.contains("jdbc:h2:mem:"), "URL must be an in-memory H2 URL: " + url);
        }
    }

    @Test
    public void testLiquibaseMigrationsExecuted() throws SQLException {
        assertNotNull(dataSource, "DataSource must be configured");

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT ID FROM DATABASECHANGELOG")) {

            Set<String> executedChangeSets = new HashSet<>();
            while (rs.next()) {
                executedChangeSets.add(rs.getString("ID"));
            }

            assertTrue(executedChangeSets.contains("01-create-document-table"));
            assertTrue(executedChangeSets.contains("02-create-natural-person-table"));
            assertTrue(executedChangeSets.contains("03-create-account-table"));
            assertTrue(executedChangeSets.contains("04-create-role-table"));
            assertTrue(executedChangeSets.contains("05-create-menu-table"));
            assertTrue(executedChangeSets.contains("05-create-panel-table"));
            assertTrue(executedChangeSets.contains("06-create-email-table"));
            assertTrue(executedChangeSets.contains("06-create-reset-password-token-table"));
            assertTrue(executedChangeSets.contains("07-create-role-accounts-table"));
            assertTrue(executedChangeSets.contains("07-create-role-panels-table"));
            assertTrue(executedChangeSets.contains("07-create-email-attachments-table"));
            assertTrue(executedChangeSets.contains("08-add-foreign-keys"));
            assertTrue(executedChangeSets.contains("08-add-indexes"));
        }
    }

    @Test
    public void testCommonPropertiesLoaded() {
        assertEquals("AppService.ir", environment.getProperty("spring.application.domain"));
        assertNotNull(environment.getProperty("regx.email"), "regx.email should be loaded");
        assertNotNull(environment.getProperty("regx.password"), "regx.password should be loaded");
        assertNotNull(environment.getProperty("regx.mobile"), "regx.mobile should be loaded");
        assertEquals("classpath:/db/changelog/db.changelog-master.yaml", environment.getProperty("spring.liquibase.change-log"));
        assertEquals("validate", environment.getProperty("spring.jpa.hibernate.ddl-auto"));
    }
}
