package ir.appservice.core.domain.entity;

import ir.appservice.core.domain.exception.DomainValidationException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Pure POJO Domain Entity: I18nMessage.
 * Dynamic database-driven localization entry for runtime hot-reloads and multi-language support.
 * Zero Spring or JPA annotations. Standard JDK 17 only.
 */
public class I18nMessage implements Serializable {

    private String id;
    private String messageKey;
    private String locale;
    private String content;
    private String category;
    private String tenantId;
    private long version;
    private Instant createdAt;
    private Instant updatedAt;

    public I18nMessage() {
        this.locale = "fa";
        this.category = "general";
        this.version = 1L;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public I18nMessage(String id, String messageKey, String locale, String content, String category, String tenantId) {
        if (messageKey == null || messageKey.trim().isEmpty()) {
            throw new DomainValidationException("messageKey", "I18n message key cannot be null or empty");
        }
        if (locale == null || locale.trim().isEmpty()) {
            throw new DomainValidationException("locale", "I18n locale cannot be null or empty");
        }
        this.id = id;
        this.messageKey = messageKey.trim();
        this.locale = locale.trim().toLowerCase();
        this.content = content != null ? content : "";
        this.category = category != null ? category.trim() : "general";
        this.tenantId = tenantId;
        this.version = 1L;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateContent(String newContent) {
        this.content = newContent != null ? newContent : "";
        this.version++;
        this.updatedAt = Instant.now();
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
        if (messageKey == null || messageKey.trim().isEmpty()) {
            throw new DomainValidationException("messageKey", "I18n message key cannot be null or empty");
        }
        this.messageKey = messageKey.trim();
        this.updatedAt = Instant.now();
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        if (locale == null || locale.trim().isEmpty()) {
            throw new DomainValidationException("locale", "I18n locale cannot be null or empty");
        }
        this.locale = locale.trim().toLowerCase();
        this.updatedAt = Instant.now();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content != null ? content : "";
        this.updatedAt = Instant.now();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
        this.updatedAt = Instant.now();
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        I18nMessage that = (I18nMessage) o;
        return Objects.equals(messageKey, that.messageKey) &&
                Objects.equals(locale, that.locale) &&
                Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageKey, locale, tenantId);
    }

    @Override
    public String toString() {
        return "I18nMessage{" +
                "id='" + id + '\'' +
                ", messageKey='" + messageKey + '\'' +
                ", locale='" + locale + '\'' +
                ", version=" + version +
                '}';
    }
}
