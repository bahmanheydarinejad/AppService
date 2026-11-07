package ir.appservice.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA Entity mapping for `account` (User Principal).
 */
@Entity
@Table(name = "account")
public class UserJpaEntity extends BaseJpaEntity {

    @Column(name = "account_name", length = 255)
    private String username;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "mobile_number", length = 255)
    private String mobileNumber;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "tenant_id", length = 255)
    private String tenantId;

    @Column(name = "failed_attempts")
    private int failedLoginAttempts = 0;

    @Column(name = "last_login_date")
    private Instant lastLoginAt;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "role_accounts",
            joinColumns = @JoinColumn(name = "accounts_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "roles_id", referencedColumnName = "id")
    )
    private Set<RoleJpaEntity> roles = new HashSet<>();

    public UserJpaEntity() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public Set<RoleJpaEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleJpaEntity> roles) {
        this.roles = roles != null ? roles : new HashSet<>();
    }
}
