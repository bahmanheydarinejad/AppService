package ir.appservice.common.exception;

/**
 * Root unchecked application exception for all AppService framework modules.
 */
public class AppServiceException extends RuntimeException {

    private final String errorCode;

    public AppServiceException(String message) {
        super(message);
        this.errorCode = "INTERNAL_ERROR";
    }

    public AppServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AppServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
