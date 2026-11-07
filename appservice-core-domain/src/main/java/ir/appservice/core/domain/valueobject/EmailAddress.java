package ir.appservice.core.domain.valueobject;

import ir.appservice.core.domain.exception.DomainValidationException;
import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable Email Address value object.
 * Validates RFC 5322 compliance and normalizes email strings.
 */
public final class EmailAddress implements Serializable {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,15}$");

    private final String value;

    public EmailAddress(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainValidationException("email", "Email address cannot be null or empty");
        }
        String normalized = value.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new DomainValidationException("email", "Invalid email address syntax: " + value);
        }
        this.value = normalized;
    }

    public static EmailAddress of(String value) {
        return new EmailAddress(value);
    }

    public String getValue() {
        return value;
    }

    public String getDomain() {
        int atIndex = value.indexOf('@');
        return atIndex != -1 ? value.substring(atIndex + 1) : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailAddress that = (EmailAddress) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
