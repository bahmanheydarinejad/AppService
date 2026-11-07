package ir.appservice.core.domain.valueobject;

import ir.appservice.core.domain.exception.DomainValidationException;
import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable phone number value object.
 * Normalizes Iranian domestic numbers (09xxxxxxxxx -> +989xxxxxxxxx) and standard E.164 formats.
 */
public final class PhoneNumber implements Serializable {

    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{6,14}$");
    private static final Pattern IRAN_LOCAL_PATTERN = Pattern.compile("^0?9\\d{9}$");

    private final String e164Value;

    public PhoneNumber(String rawNumber) {
        if (rawNumber == null || rawNumber.trim().isEmpty()) {
            throw new DomainValidationException("phoneNumber", "Phone number cannot be null or empty");
        }
        String clean = rawNumber.trim().replaceAll("[\\s-()]", "");

        if (IRAN_LOCAL_PATTERN.matcher(clean).matches()) {
            if (clean.startsWith("0")) {
                this.e164Value = "+98" + clean.substring(1);
            } else {
                this.e164Value = "+98" + clean;
            }
        } else if (E164_PATTERN.matcher(clean).matches()) {
            this.e164Value = clean;
        } else {
            throw new DomainValidationException("phoneNumber",
                    "Invalid phone number format. Must conform to E.164 or Iranian 09xx format: " + rawNumber);
        }
    }

    public static PhoneNumber of(String rawNumber) {
        return new PhoneNumber(rawNumber);
    }

    public String getValue() {
        return e164Value;
    }

    public boolean isIranian() {
        return e164Value.startsWith("+98");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return Objects.equals(e164Value, that.e164Value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(e164Value);
    }

    @Override
    public String toString() {
        return e164Value;
    }
}
