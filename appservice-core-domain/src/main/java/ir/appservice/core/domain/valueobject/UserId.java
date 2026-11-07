package ir.appservice.core.domain.valueobject;

import ir.appservice.core.domain.exception.DomainValidationException;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable identifier for a User domain entity.
 */
public final class UserId implements Serializable, Comparable<UserId> {

    private final String value;

    public UserId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainValidationException("userId", "UserId value cannot be null or empty");
        }
        this.value = value.trim();
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int compareTo(UserId o) {
        return this.value.compareTo(o.value);
    }
}
