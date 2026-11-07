package ir.appservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Composition Root and Spring Boot bootstrap application entry point
 * for AppService Hexagonal Architecture.
 *
 * Scans all submodules under {@code ir.appservice}:
 * - Core Domain & SPI Ports
 * - Common Shared Contexts
 * - Infrastructure (Persistence, Security OAuth2.1, i18n, Platform/Quartz/SMS/Mail/MCP)
 * - Inbound Driving REST Controllers & OpenAPI 3.0
 * - Inbound Driving PrimeFaces 14 Single-Page Administration Console
 */
@SpringBootApplication(scanBasePackages = "ir.appservice")
@EnableAsync
@EnableScheduling
public class AppServiceApplication extends SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(AppServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AppServiceApplication.class, args);
        log.info("==================================================================");
        log.info(" AppService Hexagonal Architecture Application Successfully Started");
        log.info(" REST API Endpoints: http://localhost:8080/api/admin/**");
        log.info(" OpenAPI Spec:        http://localhost:8080/v3/api-docs");
        log.info(" PrimeFaces Console:  http://localhost:8080/dashboard.xhtml");
        log.info("==================================================================");
    }
}
