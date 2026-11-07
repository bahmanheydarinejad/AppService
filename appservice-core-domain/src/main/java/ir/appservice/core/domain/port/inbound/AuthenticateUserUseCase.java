package ir.appservice.core.domain.port.inbound;

import ir.appservice.core.domain.entity.User;
import ir.appservice.core.domain.enums.Outcome;

/**
 * Inbound Port (Primary / Driving Use Case): User Authentication Contract.
 * Pure Java interface defining application boundary.
 */
public interface AuthenticateUserUseCase {

    /**
     * Executes authentication against domain credentials and tenant context.
     *
     * @param command authentication request details
     * @return result indicating success, principal, or failure reason
     */
    AuthenticationResult authenticate(AuthenticateCommand command);

    /**
     * Authentication input command record.
     */
    record AuthenticateCommand(
            String username,
            String rawPassword,
            String tenantId,
            String clientIp,
            String userAgent
    ) {
        public AuthenticateCommand {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("Username is required for authentication");
            }
            if (rawPassword == null || rawPassword.isEmpty()) {
                throw new IllegalArgumentException("Password is required for authentication");
            }
            if (tenantId == null || tenantId.trim().isEmpty()) {
                tenantId = "default";
            }
        }
    }

    /**
     * Authentication result value record.
     */
    record AuthenticationResult(
            boolean success,
            User user,
            String token,
            String errorMessage,
            Outcome outcome
    ) {
        public static AuthenticationResult successful(User user, String token) {
            return new AuthenticationResult(true, user, token, null, Outcome.SUCCESS);
        }

        public static AuthenticationResult failed(String errorMessage, Outcome outcome) {
            return new AuthenticationResult(false, null, null, errorMessage, outcome);
        }
    }
}
