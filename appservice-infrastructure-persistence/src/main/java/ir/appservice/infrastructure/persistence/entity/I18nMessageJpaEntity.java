package ir.appservice.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA Entity: I18nMessageJpaEntity mapping to i18n_message table.
 */
@Entity
@Table(name = "i18n_message")
public class I18nMessageJpaEntity implements Serializable {

    @Id
    @Column(name = "id", nullable = false, length = 255)
    private String id;

    @Column(name = "message_key", nullable = false, length = 255)
    private String messageKey;

    @Column(name = "locale", nullable = false, length = 32)
    private String locale;

    @Lob
    @Column(name = "message_text", nullable = false)
    private String messageText;

    @Column(name = "category", length = 64)
    private String category = "general";

    @Column(name = "tenant_id", length = 255)
    private String tenantId;

    @Column(name = "direction", length = 8)
    private String direction = "ltr";

    @Column(name = "version", nullable = false)
    private long version = 1L;

    @Column(name = "registered_date")
    private Instant registeredDate;

    @Column(name = "updated_date")
    private Instant updatedDate;

    public I18nMessageJpaEntity() {
        this.id = UUID.randomUUID().toString();
    }

    public I18nMessageJpaEntity(String messageKey, String locale, String messageText, String category, String tenantId) {
        this.id = UUID.randomUUID().toString();
        this.messageKey = messageKey;
        this.locale = locale;
        this.messageText = messageText;
        this.category = category;
        this.tenantId = tenantId;
    }

    @PrePersist
    protected void onPrePersist() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        Instant now = Instant.now();
        if (this.registeredDate == null) {
            this.registeredDate = now;
        }
        this.updatedDate = now;
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.updatedDate = Instant.now();
        this.version++;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Instant getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(Instant registeredDate) {
        this.registeredDate = registeredDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        I18nMessageJpaEntity that = (I18nMessageJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
