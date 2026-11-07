package ir.appservice.core.domain.port.outbound;

import ir.appservice.core.domain.entity.Tenant;
import ir.appservice.core.domain.enums.IsolationStrategy;
import java.util.List;
import java.util.Optional;

/**
 * Outbound Port (SPI): Tenant Persistence & Provisioning Boundary Contract.
 * Pure Java interface defining multi-tenant repository operations.
 */
public interface TenantRepositoryPort {

    Optional<Tenant> findById(String id);

    Optional<Tenant> findByTenantCode(String tenantCode);

    List<Tenant> findAllActive();

    Tenant save(Tenant tenant);

    void updateIsolationStrategy(String tenantId, IsolationStrategy strategy, String schemaName, String dbUrl);

    boolean existsByTenantCode(String tenantCode);
}
