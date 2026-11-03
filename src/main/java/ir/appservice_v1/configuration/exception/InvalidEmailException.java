package ir.appservice_v1.configuration.exception;

public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(String email) {
        super(String.format("Email [%s] is invalid.", email));
    }
}
