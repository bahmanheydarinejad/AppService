package ir.appservice_v1.configuration.exception;

public class NotFoundEntityException extends ModelLayerException {
    public NotFoundEntityException(String message) {
        super(message);
    }
}
