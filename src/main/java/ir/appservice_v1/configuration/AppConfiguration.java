package ir.appservice_v1.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"ir.appservice_v1.model"})
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = {"ir.appservice_v1.model"})
@org.springframework.context.annotation.ComponentScan(basePackages = {"ir.appservice_v1"})
public class AppConfiguration implements WebMvcConfigurer {

    private final Logger logger = LoggerFactory.getLogger(AppConfiguration.class);

    @Value("classpath*:META-INF/resources/**/*.xhtml")
    private Resource[] xhtmlResources;

    @Autowired
    private Environment env;

    @Bean
    public static ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("/index.xhtml");
        registry.addViewController("/index").setViewName("/index.xhtml");
        registry.addViewController("/signin").setViewName("/signin.xhtml");
        registry.addViewController("/dashboard").setViewName("/dashboard.xhtml");
        registry.addViewController("/resetPassword").setViewName("/resetPassword.xhtml");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Bean
    public Pattern emailPattern() {

        return Pattern.compile(env.getProperty("regx.email"));
    }

    @Bean
    public Pattern passwordPattern() {

        return Pattern.compile(env.getProperty("regx.password"));
    }

    @Bean
    public Pattern mobilePattern() {

        return Pattern.compile(env.getProperty("regx.mobile"));
    }

    @Bean
    public Map<String, String> glyphicons() throws IOException {

        Properties glyphicons = new Properties();
        glyphicons.load(getClass().getResourceAsStream("/glyphicon list.properties"));

        Map<String, String> map = new HashMap<>(); glyphicons.forEach((k, v) -> map.put(String.valueOf(k), String.valueOf(v))); return map;
    }

    @Bean
    public Map<String, String> fontAwesomeIcons() throws IOException {

        Properties fontAwesomeIcons = new Properties();
        fontAwesomeIcons.load(getClass().getResourceAsStream("/font awesome.properties"));

        Map<String, String> map = new HashMap<>(); fontAwesomeIcons.forEach((k, v) -> map.put(String.valueOf(k), String.valueOf(v))); return map;
    }

    @Bean
    public SortedMap<String, String> appIcons() throws IOException {
        SortedMap<String, String> iconMap = new TreeMap<>(fontAwesomeIcons());
        iconMap.putAll(glyphicons());

        return iconMap;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public Map<String, Resource> panelsResources() throws IOException {
        Map<String, Resource> panelsResources = new HashMap<>();
        for (Resource resource : xhtmlResources) {
            String relativePath = resource.getURL().getPath();
            relativePath = relativePath.substring(relativePath.lastIndexOf("resources")).replace(
                    "resources/", "").replace("/", "\\");
            panelsResources.put(relativePath, resource);
//            logger.trace("Resource: {}", relativePath);
        }

        return panelsResources;
    }

//    @Bean
//    public JavaMailSender appMailSender() {
//        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//        mailSender.setHost(env.getProperty("spring.mail.host"));
//        mailSender.setPort(Integer.valueOf(env.getProperty("spring.mail.port")));
//
//        mailSender.setUsername(env.getProperty("spring.mail.username"));
//        mailSender.setPassword(env.getProperty("spring.mail.password"));
//
//        Properties props = mailSender.getJavaMailProperties();
//        props.put("mail.transport.protocol", "smtp");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.debug", "true");
//
//        return mailSender;
//    }
//
//    @Bean
//    public JavaMailSender GMailSender() {
//        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//        mailSender.setHost("smtp.gmail.com");
//        mailSender.setPort(587);
//
//        mailSender.setUsername("my.gmail@gmail.com");
//        mailSender.setPassword("password");
//
//        Properties props = mailSender.getJavaMailProperties();
//        props.put("mail.transport.protocol", "smtp");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.debug", "true");
//
//        return mailSender;
//    }

}


