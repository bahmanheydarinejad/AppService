package ir.appservice.common.exception;

/**
 * Thrown when an entity creation or update violates a unique constraint or domain uniqueness invariant.
 */
public class DuplicateEntityException extends AppServiceException {

    private final String entityName;
    private final String fieldName;
    private final Object duplicateValue;

    public DuplicateEntityException(String entityName, String fieldName, Object duplicateValue) {
        super("DUPLICATE_ENTITY",
                String.format("Duplicate entity conflict: %s with %s '%s' already exists", entityName, fieldName, duplicateValue));
        this.entityName = entityName;
        this.fieldName = fieldName;
        this.duplicateValue = duplicateValue;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getDuplicateValue() {
        return duplicateValue;
    }
}
