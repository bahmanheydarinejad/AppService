package ir.appservice.core.domain.enums;

/**
 * Supported SMS Gateway Provider types for dynamic routing and failover.
 */
public enum SmsProviderType {
    KAVENEGAR("Kavenegar", false),
    MAGFA("Magfa", false),
    FARAPAYAMAK("FaraPayamak", false),
    TWILIO("Twilio", true),
    MOCK("Mock / Test Provider", true);

    private final String displayName;
    private final boolean supportsInternational;

    SmsProviderType(String displayName, boolean supportsInternational) {
        this.displayName = displayName;
        this.supportsInternational = supportsInternational;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSupportsInternational() {
        return supportsInternational;
    }
}
