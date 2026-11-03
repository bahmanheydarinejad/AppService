package ir.appservice_v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Isolated Spring Boot Application Starter for AppService V1.
 * Scans only the ir.appservice_v1 package hierarchy to guarantee runtime isolation.
 */
@SpringBootApplication(scanBasePackages = "ir.appservice_v1")
@EnableAsync
public class AppServiceV1Application extends SpringBootServletInitializer {

    private static final Logger logger = LoggerFactory.getLogger(AppServiceV1Application.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AppServiceV1Application.class, args);
        logger.info("AppService V1 Application started successfully: {}", context.getId());
    }
}
