package ir.appservice.core.domain.entity;

import ir.appservice.core.domain.enums.Status;
import ir.appservice.core.domain.exception.DomainValidationException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Pure POJO Domain Entity: Role.
 * Zero Spring or JPA annotations. Standard JDK 17 only.
 */
public class Role implements Serializable {

    private String id;
    private String name;
    private String description;
    private String tenantId;
    private Status status;
    private final Set<Permission> permissions = new HashSet<>();
    private Instant createdAt;
    private Instant updatedAt;

    public Role() {
        this.status = Status.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Role(String id, String name, String description, String tenantId) {
        if (name == null || name.trim().isEmpty()) {
            throw new DomainValidationException("name", "Role name cannot be null or empty");
        }
        this.id = id;
        this.name = name.trim().toUpperCase();
        this.description = description;
        this.tenantId = tenantId;
        this.status = Status.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void addPermission(Permission permission) {
        if (permission != null) {
            this.permissions.add(permission);
            this.updatedAt = Instant.now();
        }
    }

    public void removePermission(Permission permission) {
        if (permission != null) {
            this.permissions.remove(permission);
            this.updatedAt = Instant.now();
        }
    }

    public boolean hasPermission(String permissionCode) {
        if (permissionCode == null) return false;
        return permissions.stream()
                .anyMatch(p -> p.getCode().equalsIgnoreCase(permissionCode.trim()));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new DomainValidationException("name", "Role name cannot be null or empty");
        }
        this.name = name.trim().toUpperCase();
        this.updatedAt = Instant.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status != null && this.status != null && !this.status.canTransitionTo(status)) {
            throw new DomainValidationException("status",
                    String.format("Invalid role status transition from %s to %s", this.status, status));
        }
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public void setPermissions(Set<Permission> newPermissions) {
        this.permissions.clear();
        if (newPermissions != null) {
            this.permissions.addAll(newPermissions);
        }
        this.updatedAt = Instant.now();
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
        Role role = (Role) o;
        return Objects.equals(name, role.name) && Objects.equals(tenantId, role.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, tenantId);
    }

    @Override
    public String toString() {
        return "Role{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", status=" + status +
                ", permissionsCount=" + permissions.size() +
                '}';
    }
}
