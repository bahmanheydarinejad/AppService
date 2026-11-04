package ir.appservice.configuration;

import ir.appservice.model.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class AppSecurity {

    private static final Logger logger = LoggerFactory.getLogger(AppSecurity.class);

    private final SessionRegistry sessionRegistry;
    private final AccountService accountService;

    public AppSecurity(SessionRegistry sessionRegistry, AccountService accountService) {
        this.sessionRegistry = sessionRegistry;
        this.accountService = accountService;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(accountService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session
                .maximumSessions(100)
                .expiredUrl("/index")
                .maxSessionsPreventsLogin(false)
                .sessionRegistry(sessionRegistry)
        );

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/javax.faces.resource/**",
                        "/jakarta.faces.resource/**",
                        "/",
                        "/index", "/index.xhtml",
                        "/signin", "/signin.xhtml",
                        "/login.xhtml",   // AdminFaces default fallback — kept as permitAll safety net
                        "/signout",
                        "/error",
                        "/document/**",
                        "/resetPassword.xhtml", "/resetPassword", "/resetPassword/**"
                ).permitAll()
                .anyRequest().authenticated()
        );

        // Authentication is handled by JSF AuthenticationBean; Spring Security only
        // needs to know the login page so it redirects unauthenticated requests there
        // instead of the default /login which is not mapped.
        http.formLogin(form -> form
                .loginPage("/signin")
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/signout")
                .logoutSuccessUrl("/")
                .permitAll()
        );

        http.csrf(csrf -> csrf.disable());

        return http.build();
    }
}
