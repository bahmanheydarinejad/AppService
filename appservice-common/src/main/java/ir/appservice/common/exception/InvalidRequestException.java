package ir.appservice.common.exception;

import java.util.List;

/**
 * Thrown when incoming request payload, search filter, or command parameters fail business validation.
 */
public class InvalidRequestException extends AppServiceException {

    private final List<String> validationErrors;

    public InvalidRequestException(String message) {
        super("INVALID_REQUEST", message);
        this.validationErrors = List.of(message);
    }

    public InvalidRequestException(String message, List<String> validationErrors) {
        super("INVALID_REQUEST", message);
        this.validationErrors = validationErrors != null ? validationErrors : List.of(message);
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
