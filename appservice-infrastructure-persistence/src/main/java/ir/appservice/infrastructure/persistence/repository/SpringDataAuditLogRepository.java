package ir.appservice.infrastructure.persistence.repository;

import ir.appservice.infrastructure.persistence.entity.AuditEventJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for tamper-resistant append-only audit events.
 */
@Repository
public interface SpringDataAuditLogRepository extends JpaRepository<AuditEventJpaEntity, String>, JpaSpecificationExecutor<AuditEventJpaEntity> {

    List<AuditEventJpaEntity> findByTraceIdOrderByTimestampDesc(String traceId);

    @Query("SELECT a FROM AuditEventJpaEntity a WHERE a.tenantId = :tenantId ORDER BY a.timestamp DESC")
    List<AuditEventJpaEntity> findRecentEventsByTenant(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT a FROM AuditEventJpaEntity a WHERE a.actorId = :actor AND a.tenantId = :tenantId ORDER BY a.timestamp DESC")
    List<AuditEventJpaEntity> findByActorAndTenant(@Param("actor") String actor, @Param("tenantId") String tenantId);
}
