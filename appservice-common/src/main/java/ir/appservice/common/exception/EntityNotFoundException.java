package ir.appservice.common.exception;

/**
 * Thrown when an expected persistent entity cannot be found by its identifier.
 */
public class EntityNotFoundException extends AppServiceException {

    private final String entityName;
    private final Object entityId;

    public EntityNotFoundException(String entityName, Object entityId) {
        super("ENTITY_NOT_FOUND", String.format("Entity '%s' with identifier '%s' was not found", entityName, entityId));
        this.entityName = entityName;
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public Object getEntityId() {
        return entityId;
    }
}
