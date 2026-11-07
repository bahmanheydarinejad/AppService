package ir.appservice.common.exception;

/**
 * Thrown when an authenticated principal attempts cross-tenant data access violation.
 */
public class TenantAccessDeniedException extends AppServiceException {

    private final String currentTenant;
    private final String targetTenant;

    public TenantAccessDeniedException(String currentTenant, String targetTenant) {
        super("TENANT_ACCESS_DENIED",
                String.format("Cross-tenant access violation: active tenant '%s' cannot access resource belonging to tenant '%s'",
                        currentTenant, targetTenant));
        this.currentTenant = currentTenant;
        this.targetTenant = targetTenant;
    }

    public String getCurrentTenant() {
        return currentTenant;
    }

    public String getTargetTenant() {
        return targetTenant;
    }
}
