package ir.appservice.core.domain.entity;

import ir.appservice.core.domain.exception.DomainValidationException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Pure POJO Domain Entity: Permission.
 * Zero Spring or JPA annotations. Standard JDK 17 only.
 */
public class Permission implements Serializable {

    private String id;
    private String code;
    private String description;
    private String module;
    private Instant createdAt;

    public Permission() {
        this.createdAt = Instant.now();
    }

    public Permission(String id, String code, String description, String module) {
        if (code == null || code.trim().isEmpty()) {
            throw new DomainValidationException("code", "Permission code cannot be null or empty");
        }
        this.id = id;
        this.code = code.trim().toUpperCase();
        this.description = description;
        this.module = module;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new DomainValidationException("code", "Permission code cannot be null or empty");
        }
        this.code = code.trim().toUpperCase();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
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
        Permission that = (Permission) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", module='" + module + '\'' +
                '}';
    }
}
