package ir.appservice.infrastructure.security.rbac;

import ir.appservice.common.context.TenantContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;

/**
 * Dynamic RBAC Authority Provider for Spring Security Method Security.
 * Enables dynamic expression checks in @PreAuthorize, e.g.:
 * \@PreAuthorize("@dynamicRbac.hasPermission(authentication, 'USER_MANAGE')")
 */
@Component("dynamicRbac")
public class DynamicRbacAuthorityProvider {

    /**
     * Checks if current authentication has required permission.
     */
    public boolean hasPermission(Authentication authentication, String requiredPermission) {
        if (authentication == null || !authentication.isAuthenticated() || requiredPermission == null) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String formattedPerm = requiredPermission.startsWith("PERMISSION_") ? requiredPermission : "PERMISSION_" + requiredPermission;

        for (GrantedAuthority authority : authorities) {
            String authStr = authority.getAuthority();
            if (Objects.equals(authStr, requiredPermission) || Objects.equals(authStr, formattedPerm)) {
                return true;
            }
            // Super-admin or ROOT role overrides
            if ("ROLE_SUPER_ADMIN".equals(authStr) || "ROLE_ADMIN".equals(authStr)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if current authentication has any of the specified permissions.
     */
    public boolean hasAnyPermission(Authentication authentication, String... requiredPermissions) {
        if (requiredPermissions == null) return false;
        for (String perm : requiredPermissions) {
            if (hasPermission(authentication, perm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if current authentication has the specified role.
     */
    public boolean hasRole(Authentication authentication, String requiredRole) {
        if (authentication == null || !authentication.isAuthenticated() || requiredRole == null) {
            return false;
        }

        String formattedRole = requiredRole.startsWith("ROLE_") ? requiredRole : "ROLE_" + requiredRole;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (Objects.equals(authority.getAuthority(), formattedRole)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if current authentication has any of the specified roles.
     */
    public boolean hasAnyRole(Authentication authentication, String... requiredRoles) {
        if (requiredRoles == null) return false;
        for (String role : requiredRoles) {
            if (hasRole(authentication, role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifies that the actor's tenant context matches the target tenant ID.
     */
    public boolean isTenantAuthorized(Authentication authentication, String expectedTenantId) {
        if (authentication == null || !authentication.isAuthenticated() || expectedTenantId == null) {
            return false;
        }

        // Check if user has global platform admin role
        if (hasRole(authentication, "SUPER_ADMIN")) {
            return true;
        }

        // Check JWT claim
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String tokenTenant = jwtAuth.getToken().getClaimAsString("tenant_id");
            if (expectedTenantId.equals(tokenTenant)) {
                return true;
            }
        }

        // Fallback to active TenantContextHolder
        String activeTenant = TenantContextHolder.getTenantId();
        return expectedTenantId.equals(activeTenant);
    }
}
