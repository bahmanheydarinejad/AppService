package ir.appservice.api.rest.controller;

import ir.appservice.common.api.ApiResponse;
import ir.appservice.common.exception.EntityNotFoundException;
import ir.appservice.infrastructure.security.oauth2.OAuth2ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * REST Endpoint for OAuth2 Client Registration, PKCE Enforcement, and Secret Key Rotation.
 */
@RestController
@RequestMapping("/api/admin/clients")
public class OAuth2ClientManagementController {

    private final OAuth2ClientService clientService;

    public OAuth2ClientManagementController(OAuth2ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CLIENTS_MANAGE')")
    public ResponseEntity<ApiResponse<OAuth2ClientService.ClientRegistrationResult>> registerClient(
            @RequestBody Map<String, Object> payload) {

        String clientName = (String) payload.getOrDefault("clientName", "AppService-Client");
        @SuppressWarnings("unchecked")
        Set<String> scopes = payload.containsKey("scopes")
                ? Set.copyOf((java.util.Collection<String>) payload.get("scopes"))
                : Set.of("openid", "profile");
        @SuppressWarnings("unchecked")
        Set<String> grantTypes = payload.containsKey("grantTypes")
                ? Set.copyOf((java.util.Collection<String>) payload.get("grantTypes"))
                : Set.of("authorization_code", "refresh_token");
        @SuppressWarnings("unchecked")
        Set<String> redirectUris = payload.containsKey("redirectUris")
                ? Set.copyOf((java.util.Collection<String>) payload.get("redirectUris"))
                : Set.of("https://oauth.appservice.internal/callback");
        boolean requirePkce = !Boolean.FALSE.equals(payload.get("requirePkce"));
        int rateLimit = payload.containsKey("rateLimit") ? ((Number) payload.get("rateLimit")).intValue() : 120;
        String tenantId = (String) payload.getOrDefault("tenantId", "system");

        OAuth2ClientService.ClientRegistrationResult result = clientService.registerClient(
                clientName, scopes, grantTypes, redirectUris, requirePkce, rateLimit, tenantId
        );

        return ResponseEntity.ok(ApiResponse.created(result, "OAuth2 client registered successfully"));
    }

    @PostMapping("/{id}/keys/rotate")
    @PreAuthorize("hasAuthority('CLIENTS_MANAGE')")
    public ResponseEntity<ApiResponse<OAuth2ClientService.ClientRegistrationResult>> rotateKeys(@PathVariable("id") String id) {
        OAuth2ClientService.ClientRegistrationResult result = clientService.rotateSecret(id)
                .orElseThrow(() -> new EntityNotFoundException("OAuth2Client", id));

        return ResponseEntity.ok(ApiResponse.success(result, "OAuth2 client secret rotated successfully"));
    }
}
