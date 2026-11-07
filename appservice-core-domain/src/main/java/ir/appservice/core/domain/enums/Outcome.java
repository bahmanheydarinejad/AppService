package ir.appservice.core.domain.enums;

/**
 * Universal outcome of operations, audit trails, and command executions.
 */
public enum Outcome {
    SUCCESS,
    FAILURE,
    WARNING,
    BLOCKED,
    REJECTED,
    TIMEOUT;

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean isFailure() {
        return this == FAILURE || this == TIMEOUT || this == BLOCKED || this == REJECTED;
    }

    public boolean requiresAlert() {
        return this == BLOCKED || this == FAILURE;
    }
}
