package ir.appservice.core.domain.enums;

/**
 * Multi-tenant isolation architecture strategy.
 * Governs data segregation across Shared Database, Schema-per-tenant, or Dedicated Database.
 */
public enum IsolationStrategy {
    /**
     * Tier 1: Shared Database with tenant discriminator column.
     * Lowest resource cost, ideal for lightweight/standard tenants.
     */
    SHARED_DB("SHARED_DB", "Shared Database with Column Discriminator", 1),

    /**
     * Tier 2: Dedicated PostgreSQL Schema within a shared database instance.
     * Moderate isolation and dedicated table namespaces per tenant.
     */
    POSTGRESQL_SCHEMA("POSTGRESQL_SCHEMA", "PostgreSQL Dedicated Schema", 2),

    /**
     * Tier 3: Dedicated Physical Database.
     * Maximum enterprise isolation, separate connection pool and credentials.
     */
    DEDICATED_DB("DEDICATED_DB", "Dedicated Physical Database Instance", 3);

    private final String code;
    private final String description;
    private final int tierLevel;

    IsolationStrategy(String code, String description, int tierLevel) {
        this.code = code;
        this.description = description;
        this.tierLevel = tierLevel;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getTierLevel() {
        return tierLevel;
    }

    public boolean requiresSchemaCreation() {
        return this == POSTGRESQL_SCHEMA;
    }

    public boolean requiresDedicatedDatasource() {
        return this == DEDICATED_DB;
    }

    public static IsolationStrategy fromCode(String code) {
        if (code == null) {
            return SHARED_DB;
        }
        for (IsolationStrategy strategy : values()) {
            if (strategy.code.equalsIgnoreCase(code.trim())) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unknown IsolationStrategy code: " + code);
    }
}
