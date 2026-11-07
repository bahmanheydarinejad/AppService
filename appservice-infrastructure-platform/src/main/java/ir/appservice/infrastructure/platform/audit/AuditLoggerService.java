package ir.appservice.infrastructure.platform.audit;

import ir.appservice.common.context.TenantContextHolder;
import ir.appservice.common.context.TraceContextHolder;
import ir.appservice.core.domain.entity.AuditEvent;
import ir.appservice.core.domain.enums.Outcome;
import ir.appservice.core.domain.port.outbound.AuditLogPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service orchestrating tamper-resistant audit logging with automatic credential masking.
 */
@Service
public class AuditLoggerService {

    private final AuditLogPort auditLogPort;
    private final AuditDiffSanitizer sanitizer = new AuditDiffSanitizer();

    public AuditLoggerService(AuditLogPort auditLogPort) {
        this.auditLogPort = auditLogPort;
    }

    /**
     * Records an immutable audit log entry with optional sanitized JSON before/after state diffs.
     */
    public AuditEvent logAction(String actor,
                                String actionVerb,
                                String entityType,
                                String entityId,
                                Object beforeState,
                                Object afterState,
                                Outcome outcome,
                                String ipAddress) {
        String traceId = TraceContextHolder.getTraceId();
        String tenantId = TenantContextHolder.getTenantId();

        String sanitizedBefore = sanitizer.sanitizeToJson(beforeState);
        String sanitizedAfter = sanitizer.sanitizeToJson(afterState);

        AuditEvent event = new AuditEvent(
                traceId,
                actor != null ? actor : "system",
                actionVerb,
                entityType,
                entityId,
                outcome != null ? outcome : Outcome.SUCCESS,
                ipAddress,
                tenantId
        );

        event.setId(UUID.randomUUID().toString());
        event.setBeforeState(sanitizedBefore);
        event.setAfterState(sanitizedAfter);

        if (auditLogPort != null) {
            auditLogPort.recordEvent(event);
        }

        return event;
    }

    public AuditDiffSanitizer getSanitizer() {
        return sanitizer;
    }
}
