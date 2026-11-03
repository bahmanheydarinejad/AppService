package ir.appservice_v1.configuration;

import ir.appservice_v1.model.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;

@Configuration
@EnableWebSecurity
public class AppSecurity extends WebSecurityConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AppSecurity.class);

    private SessionRegistry sessionRegistry;
    private AccountService accountService;

    public AppSecurity(SessionRegistry sessionRegistry, AccountService accountService) {
        this.sessionRegistry = sessionRegistry;
        this.accountService = accountService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(accountService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement()
                .maximumSessions(100)
                .expiredUrl("/index")
                .maxSessionsPreventsLogin(false)
                .sessionRegistry(sessionRegistry);

        http
                .authorizeRequests()
                .antMatchers(
                        "/javax.faces.resource/**", "/",
                        "/index", "/index.xhtml",
                        "/signout",
                        "/error",
                        "/document/**/*",
                        "/resetPassword.xhtml", "/resetPassword", "/resetPassword/**/*"
                ).permitAll()
                .anyRequest().authenticated()

                .and()
                .formLogin()
                .disable()

                .logout()
                .logoutUrl("/signout")
                .logoutSuccessUrl("/")
                .permitAll()

                .and()
                .csrf().disable();

//        http.authorizeRequests().antMatchers("/home/**/*").hasRole("USER");
//        http.authorizeRequests().antMatchers("/admin/**/*").hasRole("ADMINISTRATOR");
    }


}
