package ir.appservice.core.domain.port.outbound;

import ir.appservice.core.domain.entity.AuditEvent;
import java.util.List;

/**
 * Outbound Port (SPI): Audit Log Persistence Contract.
 * Pure Java interface defining append-only audit event logging.
 */
public interface AuditLogPort {

    void recordEvent(AuditEvent event);

    List<AuditEvent> findByTraceId(String traceId);

    List<AuditEvent> findRecentEvents(int limit, String tenantId);

    List<AuditEvent> findByActor(String actor, String tenantId);
}
