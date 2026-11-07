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
 * Pure POJO Domain Entity: User aggregate.
 * Zero Spring or JPA annotations. Standard JDK 17 only.
 */
public class User implements Serializable {

    private String id;
    private String username;
    private String passwordHash;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Status status;
    private String tenantId;
    private int failedLoginAttempts;
    private Instant lastLoginAt;
    private final Set<Role> roles = new HashSet<>();
    private Instant createdAt;
    private Instant updatedAt;

    public User() {
        this.status = Status.ACTIVE;
        this.failedLoginAttempts = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public User(String id, String username, String passwordHash, String email, String tenantId) {
        if (username == null || username.trim().isEmpty()) {
            throw new DomainValidationException("username", "Username cannot be null or empty");
        }
        this.id = id;
        this.username = username.trim().toLowerCase();
        this.passwordHash = passwordHash;
        this.email = email != null ? email.trim().toLowerCase() : null;
        this.tenantId = tenantId;
        this.status = Status.ACTIVE;
        this.failedLoginAttempts = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void addRole(Role role) {
        if (role != null) {
            this.roles.add(role);
            this.updatedAt = Instant.now();
        }
    }

    public void removeRole(Role role) {
        if (role != null) {
            this.roles.remove(role);
            this.updatedAt = Instant.now();
        }
    }

    public boolean hasRole(String roleName) {
        if (roleName == null) return false;
        return roles.stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(roleName.trim()));
    }

    public boolean hasPermission(String permissionCode) {
        if (permissionCode == null) return false;
        return roles.stream()
                .anyMatch(r -> r.hasPermission(permissionCode));
    }

    public void recordFailedLogin(int maxAllowedAttempts) {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= maxAllowedAttempts && this.status == Status.ACTIVE) {
            this.status = Status.LOCKED;
        }
        this.updatedAt = Instant.now();
    }

    public void recordSuccessfulLogin() {
        this.failedLoginAttempts = 0;
        this.lastLoginAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void activate() {
        setStatus(Status.ACTIVE);
    }

    public void suspend() {
        setStatus(Status.SUSPENDED);
    }

    public void lock() {
        setStatus(Status.LOCKED);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new DomainValidationException("username", "Username cannot be null or empty");
        }
        this.username = username.trim().toLowerCase();
        this.updatedAt = Instant.now();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        this.updatedAt = Instant.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
        this.updatedAt = Instant.now();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        this.updatedAt = Instant.now();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        this.updatedAt = Instant.now();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.updatedAt = Instant.now();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status != null && this.status != null && !this.status.canTransitionTo(status)) {
            throw new DomainValidationException("status",
                    String.format("Invalid user status transition from %s to %s", this.status, status));
        }
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public void setRoles(Set<Role> newRoles) {
        this.roles.clear();
        if (newRoles != null) {
            this.roles.addAll(newRoles);
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
        User user = (User) o;
        return Objects.equals(username, user.username) && Objects.equals(tenantId, user.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, tenantId);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", status=" + status +
                '}';
    }
}
