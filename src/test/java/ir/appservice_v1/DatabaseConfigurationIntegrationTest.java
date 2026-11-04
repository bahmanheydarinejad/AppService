package ir.appservice_v1;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Run.class)
@EnableAutoConfiguration(excludeName = {
        "org.joinfaces.autoconfigure.javaxfaces.JsfBeansAutoConfiguration",
        "org.joinfaces.autoconfigure.adminfaces.AdminfacesAutoConfiguration"
})
public class DatabaseConfigurationIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    @Test
    public void testH2InMemoryDatabaseConfiguration() throws SQLException {
        Assert.assertNotNull("DataSource must be configured", dataSource);

        try (Connection connection = dataSource.getConnection()) {
            Assert.assertNotNull("Database connection should not be null", connection);
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            String url = metaData.getURL();

            Assert.assertEquals("Database product must be H2", "H2", databaseProductName);
            Assert.assertTrue("URL must be an in-memory H2 URL: " + url, url.contains("jdbc:h2:mem:"));
        }
    }

    @Test
    public void testCommonPropertiesLoaded() {
        Assert.assertEquals("AppService.ir", environment.getProperty("spring.application.domain"));
        Assert.assertNotNull("regx.email should be loaded", environment.getProperty("regx.email"));
        Assert.assertNotNull("regx.password should be loaded", environment.getProperty("regx.password"));
        Assert.assertNotNull("regx.mobile should be loaded", environment.getProperty("regx.mobile"));
    }
}
