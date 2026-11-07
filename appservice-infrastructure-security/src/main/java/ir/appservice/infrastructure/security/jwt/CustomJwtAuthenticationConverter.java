package ir.appservice.infrastructure.security.jwt;

import ir.appservice.common.context.TenantContextHolder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Custom JWT Authentication Converter.
 * Parses JWT claims into Spring Security GrantedAuthorities (roles, permissions, and scopes)
 * and extracts tenant context into TenantContextHolder.
 */
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        // Bind tenant ID into TenantContextHolder if present in JWT claims
        String tenantId = jwt.getClaimAsString("tenant_id");
        if (tenantId != null && !tenantId.isBlank()) {
            TenantContextHolder.setTenantId(tenantId);
        }

        String principalClaimName = jwt.getSubject();
        if (principalClaimName == null || principalClaimName.isBlank()) {
            principalClaimName = jwt.getClaimAsString("preferred_username");
        }
        if (principalClaimName == null || principalClaimName.isBlank()) {
            principalClaimName = jwt.getClaimAsString("client_id");
        }

        return new JwtAuthenticationToken(jwt, authorities, principalClaimName);
    }

    public Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1. Roles (e.g., ["ADMIN", "TENANT_ADMIN"])
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            for (String role : roles) {
                if (role != null && !role.isBlank()) {
                    String formattedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    authorities.add(new SimpleGrantedAuthority(formattedRole));
                }
            }
        }

        // 2. Fine-grained Permissions (e.g., ["USER_READ", "USER_WRITE"])
        List<String> permissions = jwt.getClaimAsStringList("permissions");
        if (permissions != null) {
            for (String perm : permissions) {
                if (perm != null && !perm.isBlank()) {
                    String formattedPerm = perm.startsWith("PERMISSION_") ? perm : "PERMISSION_" + perm;
                    authorities.add(new SimpleGrantedAuthority(formattedPerm));
                    authorities.add(new SimpleGrantedAuthority(perm)); // Direct permission matching
                }
            }
        }

        // 3. OAuth2 Scopes (e.g., "read write" or ["read", "write"])
        Object scopeClaim = jwt.getClaim("scope");
        if (scopeClaim == null) {
            scopeClaim = jwt.getClaim("scp");
        }
        if (scopeClaim instanceof String scopeStr) {
            for (String scope : scopeStr.split(" ")) {
                if (!scope.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
                }
            }
        } else if (scopeClaim instanceof Collection<?> scopeList) {
            for (Object scope : scopeList) {
                if (scope != null && !scope.toString().isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope.toString()));
                }
            }
        }

        // 4. Fallback 'authorities' claim
        List<String> explicitAuthorities = jwt.getClaimAsStringList("authorities");
        if (explicitAuthorities != null) {
            for (String auth : explicitAuthorities) {
                if (auth != null && !auth.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority(auth));
                }
            }
        }

        return authorities;
    }
}
