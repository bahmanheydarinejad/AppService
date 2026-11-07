package ir.appservice.api;

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
 * Dedicated API Service entry point for AppService.
 *
 * Exposes:
 * - Dynamic CQRS Search REST Endpoints (/api/admin/{subject}/search)
 * - Management & Control Plane Endpoints (Scheduler, SMS, Mail, Tenants, Clients, Ops)
 * - Model Context Protocol (MCP) AI Agent Tools (/api/admin/ai/**)
 * - OpenAPI 3.0 Documentation (/v3/api-docs)
 */
@SpringBootApplication(scanBasePackages = "ir.appservice")
@EnableJpaRepositories(basePackages = "ir.appservice.infrastructure.persistence.repository")
@EntityScan(basePackages = "ir.appservice.infrastructure.persistence.entity")
@EnableAsync
@EnableScheduling
public class AppServiceApiApplication extends SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(AppServiceApiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AppServiceApiApplication.class, args);
        log.info("==================================================================");
        log.info(" AppService REST API Service Successfully Started");
        log.info(" REST Endpoints: http://localhost:8081/api/admin/**");
        log.info(" OpenAPI Spec:   http://localhost:8081/v3/api-docs");
        log.info(" Actuator:       http://localhost:8081/actuator/health");
        log.info("==================================================================");
    }
}
