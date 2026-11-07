package ir.appservice.infrastructure.security;

import com.nimbusds.jose.JWSAlgorithm;
import ir.appservice.common.context.TenantContextHolder;
import ir.appservice.infrastructure.security.jwt.CustomJwtAuthenticationConverter;
import ir.appservice.infrastructure.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OAuth2.1 Signed JWT Decoding & Verification Tests (RS256 & ES256)")
class JwtTokenSecurityTest {

    private final JwtTokenProvider tokenProvider = new JwtTokenProvider("https://auth.appservice.ir");
    private final CustomJwtAuthenticationConverter converter = new CustomJwtAuthenticationConverter();

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("RS256: Valid signed JWT decodes cleanly and extracts roles, permissions, scopes, and tenant")
    void testValidRs256Token() {
        String token = tokenProvider.createSignedToken(
                "user_bahman",
                "tenant_tehran",
                List.of("ADMIN", "USER"),
                List.of("DOCUMENT_READ", "USER_MANAGE"),
                3600L,
                JWSAlgorithm.RS256
        );

        JwtDecoder decoder = tokenProvider.createRsaJwtDecoder();
        Jwt jwt = decoder.decode(token);

        assertThat(jwt.getSubject()).isEqualTo("user_bahman");
        assertThat(jwt.getClaimAsString("tenant_id")).isEqualTo("tenant_tehran");

        AbstractAuthenticationToken auth = converter.convert(jwt);
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("user_bahman");

        // TenantContextHolder must be automatically populated from token
        assertThat(TenantContextHolder.getTenantId()).isEqualTo("tenant_tehran");

        Collection<GrantedAuthority> authorities = auth.getAuthorities();
        List<String> authNames = authorities.stream().map(GrantedAuthority::getAuthority).toList();

        assertThat(authNames).contains("ROLE_ADMIN", "ROLE_USER");
        assertThat(authNames).contains("PERMISSION_DOCUMENT_READ", "PERMISSION_USER_MANAGE");
        assertThat(authNames).contains("DOCUMENT_READ", "USER_MANAGE");
    }

    @Test
    @DisplayName("ES256: Valid Elliptic Curve signed JWT decodes cleanly")
    void testValidEs256Token() {
        String token = tokenProvider.createSignedToken(
                "service_agent_01",
                "tenant_mesh",
                List.of("AI_AGENT"),
                List.of("TOOL_EXECUTE"),
                1800L,
                JWSAlgorithm.ES256
        );

        JwtDecoder decoder = tokenProvider.createEcJwtDecoder();
        Jwt jwt = decoder.decode(token);

        assertThat(jwt.getSubject()).isEqualTo("service_agent_01");
        assertThat(jwt.getClaimAsString("tenant_id")).isEqualTo("tenant_mesh");

        AbstractAuthenticationToken auth = converter.convert(jwt);
        assertThat(auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .contains("ROLE_AI_AGENT", "PERMISSION_TOOL_EXECUTE");
    }

    @Test
    @DisplayName("Expired JWT token is rejected with validation exception (401 mapping)")
    void testExpiredTokenRejected() {
        // Token duration of -60 seconds (already expired)
        String expiredToken = tokenProvider.createSignedToken(
                "user_expired",
                "tenant_default",
                List.of("USER"),
                List.of("READ"),
                -60L,
                JWSAlgorithm.RS256
        );

        JwtDecoder decoder = tokenProvider.createRsaJwtDecoder();
        assertThatThrownBy(() -> decoder.decode(expiredToken))
                .isInstanceOfAny(JwtValidationException.class, BadJwtException.class);
    }

    @Test
    @DisplayName("Altered or tampered JWT signature is rejected with BadJwtException (401 mapping)")
    void testTamperedSignatureRejected() {
        String validToken = tokenProvider.createSignedToken(
                "user_valid",
                "tenant_default",
                List.of("USER"),
                List.of("READ"),
                3600L,
                JWSAlgorithm.RS256
        );

        // Tamper with the token signature
        String[] parts = validToken.split("\\.");
        char midChar = parts[2].charAt(10);
        char alteredChar = (midChar == 'A') ? 'B' : 'A';
        String tamperedSignature = parts[2].substring(0, 10) + alteredChar + parts[2].substring(11);
        String tamperedToken = parts[0] + "." + parts[1] + "." + tamperedSignature;

        JwtDecoder decoder = tokenProvider.createRsaJwtDecoder();
        assertThatThrownBy(() -> decoder.decode(tamperedToken))
                .isInstanceOf(BadJwtException.class);
    }

    @Test
    @DisplayName("Token signed by different/untrusted RSA key is rejected")
    void testUntrustedSignerRejected() {
        // Sign with another provider's key
        JwtTokenProvider untrustedProvider = new JwtTokenProvider("https://malicious.issuer.org");
        String untrustedToken = untrustedProvider.createSignedToken(
                "hacker",
                "tenant_stolen",
                List.of("ADMIN"),
                List.of("ALL"),
                3600L,
                JWSAlgorithm.RS256
        );

        // Decode with our application's decoder
        JwtDecoder appDecoder = tokenProvider.createRsaJwtDecoder();
        assertThatThrownBy(() -> appDecoder.decode(untrustedToken))
                .isInstanceOf(BadJwtException.class);
    }
}
