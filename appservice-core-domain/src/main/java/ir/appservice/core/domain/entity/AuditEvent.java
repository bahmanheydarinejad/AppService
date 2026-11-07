package ir.appservice.core.domain.entity;

import ir.appservice.core.domain.enums.Outcome;
import ir.appservice.core.domain.exception.DomainValidationException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Pure POJO Domain Entity: AuditEvent.
 * Tamper-resistant, immutable record of sensitive system operations.
 * Zero Spring or JPA annotations. Standard JDK 17 only.
 */
public class AuditEvent implements Serializable {

    private String id;
    private String traceId;
    private String actor;
    private String action;
    private String subject;
    private String payloadDiff;
    private Outcome outcome;
    private String ipAddress;
    private String tenantId;
    private Instant timestamp;

    public AuditEvent() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.outcome = Outcome.SUCCESS;
    }

    public AuditEvent(String traceId, String actor, String action, String subject,
                      String payloadDiff, Outcome outcome, String ipAddress, String tenantId) {
        if (action == null || action.trim().isEmpty()) {
            throw new DomainValidationException("action", "Audit action cannot be null or empty");
        }
        this.id = UUID.randomUUID().toString();
        this.traceId = traceId;
        this.actor = actor != null ? actor.trim() : "anonymous";
        this.action = action.trim().toUpperCase();
        this.subject = subject != null ? subject.trim() : "system";
        this.payloadDiff = payloadDiff;
        this.outcome = outcome != null ? outcome : Outcome.SUCCESS;
        this.ipAddress = ipAddress;
        this.tenantId = tenantId;
        this.timestamp = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        if (action == null || action.trim().isEmpty()) {
            throw new DomainValidationException("action", "Audit action cannot be null or empty");
        }
        this.action = action.trim().toUpperCase();
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPayloadDiff() {
        return payloadDiff;
    }

    public void setPayloadDiff(String payloadDiff) {
        this.payloadDiff = payloadDiff;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditEvent that = (AuditEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "AuditEvent{" +
                "id='" + id + '\'' +
                ", traceId='" + traceId + '\'' +
                ", actor='" + actor + '\'' +
                ", action='" + action + '\'' +
                ", outcome=" + outcome +
                ", timestamp=" + timestamp +
                '}';
    }
}
