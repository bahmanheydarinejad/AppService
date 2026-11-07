package ir.appservice.infrastructure.security;

import ir.appservice.infrastructure.security.rbac.DynamicRbacAuthorityProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig(DynamicRbacMethodSecurityTest.TestConfig.class)
@DisplayName("Dynamic RBAC & Spring Security Method Security (@PreAuthorize) Tests")
class DynamicRbacMethodSecurityTest {

    @Configuration
    @EnableMethodSecurity(prePostEnabled = true)
    static class TestConfig {
        @Bean
        public DynamicRbacAuthorityProvider dynamicRbac() {
            return new DynamicRbacAuthorityProvider();
        }

        @Bean
        public SampleSecuredService sampleSecuredService() {
            return new SampleSecuredService();
        }
    }

    static class SampleSecuredService {
        @PreAuthorize("hasAuthority('PERMISSION_USER_WRITE')")
        public String executeUserWrite() {
            return "WRITE_SUCCESS";
        }

        @PreAuthorize("@dynamicRbac.hasPermission(authentication, 'TENANT_MANAGE')")
        public String executeTenantManage() {
            return "TENANT_SUCCESS";
        }

        @PreAuthorize("hasRole('ADMIN')")
        public String executeAdminAction() {
            return "ADMIN_SUCCESS";
        }
    }

    @Autowired
    private SampleSecuredService securedService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Authorized user with PERMISSION_USER_WRITE successfully executes protected method")
    void testAuthorizedMethodExecution() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "authorized_user",
                "N/A",
                List.of(new SimpleGrantedAuthority("PERMISSION_USER_WRITE"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        String result = securedService.executeUserWrite();
        assertThat(result).isEqualTo("WRITE_SUCCESS");
    }

    @Test
    @DisplayName("Unauthorized user without required permission is blocked with AccessDeniedException (403 Forbidden)")
    void testUnauthorizedMethodBlocked() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "unauthorized_user",
                "N/A",
                List.of(new SimpleGrantedAuthority("PERMISSION_USER_READ"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThatThrownBy(() -> securedService.executeUserWrite())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Dynamic RBAC SpEL evaluator allows user with dynamic TENANT_MANAGE permission")
    void testDynamicRbacSpelEvaluator() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "operator",
                "N/A",
                List.of(new SimpleGrantedAuthority("TENANT_MANAGE"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        String result = securedService.executeTenantManage();
        assertThat(result).isEqualTo("TENANT_SUCCESS");
    }

    @Test
    @DisplayName("Dynamic RBAC SpEL evaluator blocks user lacking TENANT_MANAGE permission")
    void testDynamicRbacSpelEvaluatorForbidden() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "operator",
                "N/A",
                List.of(new SimpleGrantedAuthority("DOCUMENT_READ"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThatThrownBy(() -> securedService.executeTenantManage())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("ROLE_ADMIN super-user bypasses fine-grained permission checks")
    void testAdminRoleBypass() {
        UsernamePasswordAuthenticationToken adminAuth = new UsernamePasswordAuthenticationToken(
                "root_admin",
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        // Admin can execute tenant management via dynamic evaluator override
        assertThat(securedService.executeTenantManage()).isEqualTo("TENANT_SUCCESS");
        assertThat(securedService.executeAdminAction()).isEqualTo("ADMIN_SUCCESS");
    }
}
