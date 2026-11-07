package ir.appservice.core.domain.valueobject;

import ir.appservice.core.domain.exception.DomainValidationException;
import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable identifier for a Tenant aggregate root.
 * Validates alphanumeric and hyphen/underscore formatting suitable for schema naming.
 */
public final class TenantId implements Serializable, Comparable<TenantId> {

    public static final String DEFAULT_TENANT = "default";
    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{2,64}$");

    private final String value;

    public TenantId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainValidationException("tenantId", "TenantId cannot be null or empty");
        }
        String normalized = value.trim().toLowerCase();
        if (!VALID_PATTERN.matcher(normalized).matches()) {
            throw new DomainValidationException("tenantId",
                    "TenantId must be 2-64 alphanumeric characters, underscores, or hyphens. Got: " + value);
        }
        this.value = normalized;
    }

    public static TenantId of(String value) {
        return new TenantId(value);
    }

    public static TenantId defaultTenant() {
        return new TenantId(DEFAULT_TENANT);
    }

    public String getValue() {
        return value;
    }

    public boolean isDefault() {
        return DEFAULT_TENANT.equals(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TenantId tenantId = (TenantId) o;
        return Objects.equals(value, tenantId.value);
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
    public int compareTo(TenantId o) {
        return this.value.compareTo(o.value);
    }
}
