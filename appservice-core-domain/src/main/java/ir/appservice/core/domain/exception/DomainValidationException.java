package ir.appservice.core.domain.exception;

/**
 * Domain exception thrown when value object or entity attribute invariants fail validation.
 */
public class DomainValidationException extends DomainException {

    private final String fieldName;

    public DomainValidationException(String fieldName, String message) {
        super(String.format("Domain validation failed on field '%s': %s", fieldName, message));
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
