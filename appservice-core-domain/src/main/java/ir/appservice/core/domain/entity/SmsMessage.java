package ir.appservice.core.domain.entity;

import ir.appservice.core.domain.enums.SmsProviderType;
import ir.appservice.core.domain.enums.Status;
import ir.appservice.core.domain.exception.DomainValidationException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Pure POJO Domain Entity: SmsMessage.
 * Represents outbound SMS payloads, delivery status, and retry metrics.
 * Zero Spring or JPA annotations. Standard JDK 17 only.
 */
public class SmsMessage implements Serializable {

    private String id;
    private String recipient;
    private String content;
    private SmsProviderType providerType;
    private Status status;
    private int retryCount;
    private long latencyMs;
    private String tenantId;
    private Instant sentAt;
    private Instant deliveredAt;
    private Instant createdAt;

    public SmsMessage() {
        this.id = UUID.randomUUID().toString();
        this.status = Status.PENDING;
        this.retryCount = 0;
        this.latencyMs = 0L;
        this.createdAt = Instant.now();
    }

    public SmsMessage(String recipient, String content, SmsProviderType providerType, String tenantId) {
        if (recipient == null || recipient.trim().isEmpty()) {
            throw new DomainValidationException("recipient", "SMS recipient phone number cannot be null or empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new DomainValidationException("content", "SMS content cannot be null or empty");
        }
        this.id = UUID.randomUUID().toString();
        this.recipient = recipient.trim();
        this.content = content.trim();
        this.providerType = providerType != null ? providerType : SmsProviderType.MOCK;
        this.status = Status.PENDING;
        this.retryCount = 0;
        this.latencyMs = 0L;
        this.tenantId = tenantId;
        this.createdAt = Instant.now();
    }

    public void markDelivered(long latencyMs) {
        this.status = Status.ACTIVE; // Delivered successfully
        this.latencyMs = latencyMs;
        this.deliveredAt = Instant.now();
    }

    public void markFailed() {
        this.status = Status.SUSPENDED; // Delivery failed
    }

    public void incrementRetry() {
        this.retryCount++;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        if (recipient == null || recipient.trim().isEmpty()) {
            throw new DomainValidationException("recipient", "SMS recipient phone number cannot be null or empty");
        }
        this.recipient = recipient.trim();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new DomainValidationException("content", "SMS content cannot be null or empty");
        }
        this.content = content.trim();
    }

    public SmsProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(SmsProviderType providerType) {
        this.providerType = providerType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SmsMessage that = (SmsMessage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SmsMessage{" +
                "id='" + id + '\'' +
                ", recipient='" + recipient + '\'' +
                ", providerType=" + providerType +
                ", status=" + status +
                ", latencyMs=" + latencyMs +
                '}';
    }
}
