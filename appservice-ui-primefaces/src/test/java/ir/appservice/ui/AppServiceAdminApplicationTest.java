package ir.appservice.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AppServiceAdminApplication.class)
@ActiveProfiles("h2")
class AppServiceAdminApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Admin Service: Spring Application Context and Hibernate Schema Validation Boots Cleanly")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }
}
