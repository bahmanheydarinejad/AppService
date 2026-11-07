package ir.appservice.core.domain.valueobject;

import ir.appservice.core.domain.exception.DomainValidationException;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable distributed trace identifier for request tracing and audit logs.
 */
public final class TraceId implements Serializable {

    private final String value;

    public TraceId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainValidationException("traceId", "TraceId cannot be null or empty");
        }
        this.value = value.trim();
    }

    public static TraceId generate() {
        return new TraceId("tr-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
    }

    public static TraceId of(String value) {
        return new TraceId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TraceId traceId = (TraceId) o;
        return Objects.equals(value, traceId.value);
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
