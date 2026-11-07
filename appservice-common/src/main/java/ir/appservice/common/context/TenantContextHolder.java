package ir.appservice.common.context;

import ir.appservice.core.domain.valueobject.TenantId;

import java.util.function.Supplier;

/**
 * ThreadLocal container propagating active Multi-Tenant context across thread execution.
 * Backs the Dynamic Routing DataSource and schema-switching mechanisms.
 */
public final class TenantContextHolder {

    private static final ThreadLocal<String> CURRENT_TENANT_ID = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void setTenantId(String tenantId) {
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            CURRENT_TENANT_ID.set(tenantId.trim().toLowerCase());
        } else {
            CURRENT_TENANT_ID.remove();
        }
    }

    public static String getTenantId() {
        return CURRENT_TENANT_ID.get();
    }

    public static String getTenantIdOrDefault() {
        String tenant = CURRENT_TENANT_ID.get();
        return (tenant != null && !tenant.isEmpty()) ? tenant : TenantId.DEFAULT_TENANT;
    }

    public static void clear() {
        CURRENT_TENANT_ID.remove();
    }

    /**
     * Executes a runnable block with a scoped tenant and guarantees deterministic cleanup.
     */
    public static void runWithTenant(String tenantId, Runnable task) {
        String previous = CURRENT_TENANT_ID.get();
        try {
            setTenantId(tenantId);
            task.run();
        } finally {
            if (previous != null) {
                CURRENT_TENANT_ID.set(previous);
            } else {
                CURRENT_TENANT_ID.remove();
            }
        }
    }

    /**
     * Executes a supplier block with a scoped tenant and guarantees deterministic cleanup.
     */
    public static <T> T supplyWithTenant(String tenantId, Supplier<T> supplier) {
        String previous = CURRENT_TENANT_ID.get();
        try {
            setTenantId(tenantId);
            return supplier.get();
        } finally {
            if (previous != null) {
                CURRENT_TENANT_ID.set(previous);
            } else {
                CURRENT_TENANT_ID.remove();
            }
        }
    }
}
