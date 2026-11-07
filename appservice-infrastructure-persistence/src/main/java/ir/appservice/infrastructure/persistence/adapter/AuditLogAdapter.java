package ir.appservice.infrastructure.persistence.adapter;

import ir.appservice.core.domain.entity.AuditEvent;
import ir.appservice.core.domain.port.outbound.AuditLogPort;
import ir.appservice.infrastructure.persistence.entity.AuditEventJpaEntity;
import ir.appservice.infrastructure.persistence.repository.SpringDataAuditLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Driven Adapter implementing Outbound Port `AuditLogPort`.
 * Bridges domain AuditEvent with Spring Data JPA persistence.
 */
@Component
public class AuditLogAdapter implements AuditLogPort {

    private final SpringDataAuditLogRepository auditLogRepository;

    public AuditLogAdapter(SpringDataAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public void recordEvent(AuditEvent domainEvent) {
        AuditEventJpaEntity entity = new AuditEventJpaEntity();
        if (domainEvent.getId() != null) {
            entity.setId(domainEvent.getId());
        }
        entity.setTraceId(domainEvent.getTraceId());
        entity.setActorId(domainEvent.getActor());
        entity.setActionVerb(domainEvent.getAction());
        entity.setEntityType(domainEvent.getSubject());
        entity.setOutcome(domainEvent.getOutcome());
        entity.setIpAddress(domainEvent.getIpAddress());
        entity.setTenantId(domainEvent.getTenantId() != null ? domainEvent.getTenantId() : "default");
        entity.setAfterState(domainEvent.getPayloadDiff());
        entity.setTimestamp(domainEvent.getTimestamp());

        auditLogRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> findByTraceId(String traceId) {
        return auditLogRepository.findByTraceIdOrderByTimestampDesc(traceId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> findRecentEvents(int limit, String tenantId) {
        PageRequest pageRequest = PageRequest.of(0, Math.max(1, limit));
        return auditLogRepository.findRecentEventsByTenant(tenantId, pageRequest).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> findByActor(String actor, String tenantId) {
        return auditLogRepository.findByActorAndTenant(actor, tenantId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private AuditEvent toDomain(AuditEventJpaEntity entity) {
        AuditEvent event = new AuditEvent(
                entity.getTraceId(),
                entity.getActorId(),
                entity.getActionVerb(),
                entity.getEntityType(),
                entity.getAfterState(),
                entity.getOutcome(),
                entity.getIpAddress(),
                entity.getTenantId()
        );
        event.setId(entity.getId());
        event.setTimestamp(entity.getTimestamp());
        return event;
    }
}
