package ir.appservice_v1.configuration.exception;

public class DuplicateEntityException extends ModelLayerException {
    public DuplicateEntityException(String message) {
        super(message);
    }
}
