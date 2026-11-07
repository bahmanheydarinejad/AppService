package ir.appservice.infrastructure.security.config;

import ir.appservice.infrastructure.security.handler.RestAccessDeniedHandler;
import ir.appservice.infrastructure.security.handler.RestAuthenticationEntryPoint;
import ir.appservice.infrastructure.security.jwt.CustomJwtAuthenticationConverter;
import ir.appservice.infrastructure.security.jwt.JwtTokenProvider;
import ir.appservice.infrastructure.security.pkce.PkceChallengeVerifier;
import ir.appservice.infrastructure.security.ratelimit.TokenBucketRateLimiter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security Core Configuration for OAuth2.1 / OIDC Resource Server and Method Security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    @ConditionalOnMissingBean
    public TokenBucketRateLimiter tokenBucketRateLimiter() {
        return new TokenBucketRateLimiter();
    }

    @Bean
    @ConditionalOnMissingBean
    public PkceChallengeVerifier pkceChallengeVerifier() {
        return new PkceChallengeVerifier();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public CustomJwtAuthenticationConverter customJwtAuthenticationConverter() {
        return new CustomJwtAuthenticationConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder(JwtTokenProvider jwtTokenProvider) {
        return jwtTokenProvider.createRsaJwtDecoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
                                                     JwtDecoder jwtDecoder,
                                                     CustomJwtAuthenticationConverter jwtConverter) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/public/**",
                                "/api/actuator/health",
                                "/api/oauth2/token",
                                "/api/oauth2/authorize"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtConverter)
                        )
                        .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                        .accessDeniedHandler(new RestAccessDeniedHandler())
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                        .accessDeniedHandler(new RestAccessDeniedHandler())
                );

        return http.build();
    }
}
