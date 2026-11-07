package ir.appservice.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Dedicated Admin Console Service entry point for AppService (PrimeFaces 14).
 *
 * Hosts:
 * - Single-Page AJAX Dashboard (/dashboard.xhtml)
 * - 8 Backing Beans & Dynamic Include Panels
 * - Theme & Locale (RTL / LTR) Switchers
 */
@SpringBootApplication(scanBasePackages = "ir.appservice")
@EnableJpaRepositories(basePackages = "ir.appservice.infrastructure.persistence.repository")
@EntityScan(basePackages = "ir.appservice.infrastructure.persistence.entity")
@EnableAsync
@EnableScheduling
public class AppServiceAdminApplication extends SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(AppServiceAdminApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AppServiceAdminApplication.class, args);
        log.info("==================================================================");
        log.info(" AppService Admin Console (PrimeFaces) Successfully Started");
        log.info(" Dashboard Console: http://localhost:8080/dashboard.xhtml");
        log.info("==================================================================");
    }
}
