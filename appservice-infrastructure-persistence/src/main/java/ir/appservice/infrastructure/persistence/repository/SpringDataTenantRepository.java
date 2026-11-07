package ir.appservice.infrastructure.persistence.repository;

import ir.appservice.infrastructure.persistence.entity.TenantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Tenant registry management with soft-delete filtering.
 */
@Repository
public interface SpringDataTenantRepository extends JpaRepository<TenantJpaEntity, String>, JpaSpecificationExecutor<TenantJpaEntity> {

    @Query("SELECT t FROM TenantJpaEntity t WHERE t.id = :id AND t.deleteDate IS NULL")
    Optional<TenantJpaEntity> findActiveById(String id);

    @Query("SELECT t FROM TenantJpaEntity t WHERE t.tenantCode = :code AND t.deleteDate IS NULL")
    Optional<TenantJpaEntity> findActiveByCode(String code);

    @Query("SELECT t FROM TenantJpaEntity t WHERE t.deleteDate IS NULL AND t.status = 'ACTIVE'")
    List<TenantJpaEntity> findAllActiveTenants();

    boolean existsByTenantCodeAndDeleteDateIsNull(String code);
}
