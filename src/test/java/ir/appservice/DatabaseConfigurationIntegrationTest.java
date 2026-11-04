package ir.appservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

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
    public void testCommonPropertiesLoaded() {
        assertEquals("AppService.ir", environment.getProperty("spring.application.domain"));
        assertNotNull(environment.getProperty("regx.email"), "regx.email should be loaded");
        assertNotNull(environment.getProperty("regx.password"), "regx.password should be loaded");
        assertNotNull(environment.getProperty("regx.mobile"), "regx.mobile should be loaded");
    }
}
