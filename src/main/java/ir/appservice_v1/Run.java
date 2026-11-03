package ir.appservice_v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "ir.appservice_v1")
@EnableAsync
public class Run extends SpringBootServletInitializer {

    private final static Logger logger = LoggerFactory.getLogger(Run.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(Run.class, args);
        logger.info("AppService V1 started successfully.");
//        System.out.println(CustomExceptionHandlerFactory.class.getName());
//        FactoryFinder.setFactory(FactoryFinder.EXCEPTION_HANDLER_FACTORY,
//                CustomExceptionHandlerFactory.class.getName());
    }

}
