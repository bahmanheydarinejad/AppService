package ir.appservice.infrastructure.persistence.adapter;

import ir.appservice.core.domain.entity.Tenant;
import ir.appservice.core.domain.enums.IsolationStrategy;
import ir.appservice.core.domain.enums.Status;
import ir.appservice.core.domain.port.outbound.TenantRepositoryPort;
import ir.appservice.infrastructure.persistence.entity.TenantJpaEntity;
import ir.appservice.infrastructure.persistence.repository.SpringDataTenantRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Driven Adapter implementing Outbound Port `TenantRepositoryPort`.
 * Bridges domain Tenant aggregate with Spring Data JPA persistence.
 */
@Component
public class TenantRepositoryAdapter implements TenantRepositoryPort {

    private final SpringDataTenantRepository tenantRepository;

    public TenantRepositoryAdapter(SpringDataTenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> findById(String id) {
        return tenantRepository.findActiveById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> findByTenantCode(String tenantCode) {
        return tenantRepository.findActiveByCode(tenantCode.trim().toLowerCase()).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tenant> findAllActive() {
        return tenantRepository.findAllActiveTenants().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Tenant save(Tenant domainTenant) {
        TenantJpaEntity entity = tenantRepository.findActiveById(domainTenant.getId())
                .orElseGet(TenantJpaEntity::new);

        if (domainTenant.getId() != null) {
            entity.setId(domainTenant.getId());
        }
        entity.setTenantCode(domainTenant.getTenantCode());
        entity.setName(domainTenant.getName());
        entity.setIsolationStrategy(domainTenant.getIsolationStrategy());
        entity.setSchemaName(domainTenant.getSchemaName());
        entity.setDatasourceUrl(domainTenant.getDbUrl());
        entity.setStatus(domainTenant.getStatus().name());
        entity.setQuotaUsers(domainTenant.getQuotaUsers());
        entity.setQuotaStorageMb(domainTenant.getQuotaStorageMb());
        entity.setKillSwitchActive(domainTenant.isKillSwitchActive());

        TenantJpaEntity saved = tenantRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void updateIsolationStrategy(String tenantId, IsolationStrategy strategy, String schemaName, String dbUrl) {
        tenantRepository.findActiveById(tenantId).ifPresent(entity -> {
            entity.setIsolationStrategy(strategy);
            entity.setSchemaName(schemaName);
            entity.setDatasourceUrl(dbUrl);
            entity.setUpdatedDate(Instant.now());
            tenantRepository.save(entity);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTenantCode(String tenantCode) {
        return tenantRepository.existsByTenantCodeAndDeleteDateIsNull(tenantCode.trim().toLowerCase());
    }

    private Tenant toDomain(TenantJpaEntity entity) {
        Tenant tenant = new Tenant(
                entity.getId(),
                entity.getTenantCode(),
                entity.getName(),
                entity.getIsolationStrategy()
        );
        tenant.setSchemaName(entity.getSchemaName());
        tenant.setDbUrl(entity.getDatasourceUrl());
        tenant.setQuotaUsers(entity.getQuotaUsers());
        tenant.setQuotaStorageMb(entity.getQuotaStorageMb());
        tenant.setKillSwitchActive(entity.isKillSwitchActive());
        if ("ACTIVE".equalsIgnoreCase(entity.getStatus())) {
            tenant.setStatus(Status.ACTIVE);
        } else {
            tenant.setStatus(Status.SUSPENDED);
        }
        tenant.setCreatedAt(entity.getRegisteredDate());
        tenant.setUpdatedAt(entity.getUpdatedDate());
        return tenant;
    }
}
