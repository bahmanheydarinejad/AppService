package ir.appservice.ui.bean;

import ir.appservice.infrastructure.persistence.entity.AuditEventJpaEntity;
import ir.appservice.infrastructure.persistence.repository.SpringDataAuditLogRepository;
import ir.appservice.infrastructure.platform.audit.AuditDiffSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean for Panel 1: Audit Log Dashboard with Trace Filters.
 */
@Component("auditLogBean")
@SessionScope
public class AuditLogBean implements Serializable {

    private final SpringDataAuditLogRepository auditLogRepository;
    private final AuditDiffSanitizer diffSanitizer;

    private String traceIdFilter;
    private String actorIdFilter;
    private AuditEventJpaEntity selectedEvent;
    private String sanitizedBeforeState;
    private String sanitizedAfterState;
    private List<AuditEventJpaEntity> auditEvents = new ArrayList<>();

    @Autowired
    public AuditLogBean(SpringDataAuditLogRepository auditLogRepository, AuditDiffSanitizer diffSanitizer) {
        this.auditLogRepository = auditLogRepository;
        this.diffSanitizer = diffSanitizer;
    }

    public void filterEvents() {
        if (auditLogRepository == null) {
            return;
        }
        if (traceIdFilter != null && !traceIdFilter.trim().isEmpty()) {
            this.auditEvents = auditLogRepository.findByTraceIdOrderByTimestampDesc(traceIdFilter.trim());
        } else if (actorIdFilter != null && !actorIdFilter.trim().isEmpty()) {
            this.auditEvents = auditLogRepository.findByActorAndTenant(actorIdFilter.trim(), "default");
        } else {
            this.auditEvents = auditLogRepository.findAll();
        }
    }

    public void selectEvent(AuditEventJpaEntity event) {
        this.selectedEvent = event;
        if (event != null && diffSanitizer != null) {
            this.sanitizedBeforeState = diffSanitizer.sanitizeToJson(event.getBeforeState());
            this.sanitizedAfterState = diffSanitizer.sanitizeToJson(event.getAfterState());
        } else {
            this.sanitizedBeforeState = "{}";
            this.sanitizedAfterState = "{}";
        }
    }

    public void clearFilters() {
        this.traceIdFilter = null;
        this.actorIdFilter = null;
        this.selectedEvent = null;
        filterEvents();
    }

    // Getters and Setters
    public String getTraceIdFilter() { return traceIdFilter; }
    public void setTraceIdFilter(String traceIdFilter) { this.traceIdFilter = traceIdFilter; }

    public String getActorIdFilter() { return actorIdFilter; }
    public void setActorIdFilter(String actorIdFilter) { this.actorIdFilter = actorIdFilter; }

    public AuditEventJpaEntity getSelectedEvent() { return selectedEvent; }
    public void setSelectedEvent(AuditEventJpaEntity selectedEvent) { selectEvent(selectedEvent); }

    public String getSanitizedBeforeState() { return sanitizedBeforeState; }
    public String getSanitizedAfterState() { return sanitizedAfterState; }

    public List<AuditEventJpaEntity> getAuditEvents() { return auditEvents; }
    public void setAuditEvents(List<AuditEventJpaEntity> auditEvents) { this.auditEvents = auditEvents; }
}
