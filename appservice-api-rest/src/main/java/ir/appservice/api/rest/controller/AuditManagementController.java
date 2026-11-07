package ir.appservice.api.rest.controller;

import ir.appservice.common.api.ApiResponse;
import ir.appservice.common.exception.EntityNotFoundException;
import ir.appservice.infrastructure.persistence.entity.AuditEventJpaEntity;
import ir.appservice.infrastructure.persistence.repository.SpringDataAuditLogRepository;
import ir.appservice.infrastructure.platform.audit.AuditDiffSanitizer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for Audit Event management and sanitized before/after diffs.
 */
@RestController
@RequestMapping("/api/admin/audit-events")
public class AuditManagementController {

    private final SpringDataAuditLogRepository auditLogRepository;
    private final AuditDiffSanitizer diffSanitizer;

    public AuditManagementController(SpringDataAuditLogRepository auditLogRepository, AuditDiffSanitizer diffSanitizer) {
        this.auditLogRepository = auditLogRepository;
        this.diffSanitizer = diffSanitizer;
    }

    @GetMapping("/{id}/diff")
    @PreAuthorize("hasAuthority('AUDIT_READ')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuditDiff(@PathVariable("id") String id) {
        AuditEventJpaEntity event = auditLogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AuditEvent", id));

        String sanitizedBefore = diffSanitizer.sanitizeToJson(event.getBeforeState());
        String sanitizedAfter = diffSanitizer.sanitizeToJson(event.getAfterState());

        Map<String, Object> diff = Map.of(
                "id", event.getId(),
                "action", event.getActionVerb() != null ? event.getActionVerb() : "",
                "traceId", event.getTraceId() != null ? event.getTraceId() : "",
                "timestamp", event.getTimestamp() != null ? event.getTimestamp().toString() : "",
                "beforeState", sanitizedBefore != null ? sanitizedBefore : "{}",
                "afterState", sanitizedAfter != null ? sanitizedAfter : "{}"
        );

        return ResponseEntity.ok(ApiResponse.success(diff, "Audit diff retrieved successfully"));
    }
}
