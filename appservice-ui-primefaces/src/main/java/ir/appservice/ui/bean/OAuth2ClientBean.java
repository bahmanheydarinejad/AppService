package ir.appservice.ui.bean;

import ir.appservice.infrastructure.security.oauth2.OAuth2ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

/**
 * Backing bean for Panel 7: OAuth2 Client & API Key Management Panel.
 */
@Component("oauth2ClientBean")
@SessionScope
public class OAuth2ClientBean implements Serializable {

    private final OAuth2ClientService clientService;

    private String newClientName = "AppService-WebPortal";
    private boolean requirePkce = true;
    private int rateLimit = 120;
    private String tenantId = "default";
    private String createdClientId;
    private String createdClientSecret;
    private String rotatedClientSecret;
    private String statusMessage;

    @Autowired
    public OAuth2ClientBean(OAuth2ClientService clientService) {
        this.clientService = clientService;
    }

    public void registerClient() {
        if (clientService != null && newClientName != null) {
            try {
                OAuth2ClientService.ClientRegistrationResult result = clientService.registerClient(
                        newClientName,
                        Set.of("openid", "profile"),
                        Set.of("authorization_code", "refresh_token"),
                        Set.of("https://oauth.appservice.internal/callback"),
                        requirePkce,
                        rateLimit,
                        tenantId
                );
                this.createdClientId = result.clientId();
                this.createdClientSecret = result.rawClientSecret();
                this.statusMessage = "Client registered successfully! Client ID: " + createdClientId;
            } catch (Exception e) {
                this.statusMessage = "Registration failed: " + e.getMessage();
            }
        }
    }

    public void rotateSecret(String clientId) {
        if (clientService != null && clientId != null) {
            Optional<OAuth2ClientService.ClientRegistrationResult> resultOpt = clientService.rotateSecret(clientId);
            if (resultOpt.isPresent()) {
                this.rotatedClientSecret = resultOpt.get().rawClientSecret();
                this.statusMessage = "Secret rotated successfully for: " + clientId;
            } else {
                this.statusMessage = "Client not found: " + clientId;
            }
        }
    }

    // Getters and Setters
    public String getNewClientName() { return newClientName; }
    public void setNewClientName(String newClientName) { this.newClientName = newClientName; }
    public boolean isRequirePkce() { return requirePkce; }
    public void setRequirePkce(boolean requirePkce) { this.requirePkce = requirePkce; }
    public int getRateLimit() { return rateLimit; }
    public void setRateLimit(int rateLimit) { this.rateLimit = rateLimit; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCreatedClientId() { return createdClientId; }
    public String getCreatedClientSecret() { return createdClientSecret; }
    public String getRotatedClientSecret() { return rotatedClientSecret; }
    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
}
