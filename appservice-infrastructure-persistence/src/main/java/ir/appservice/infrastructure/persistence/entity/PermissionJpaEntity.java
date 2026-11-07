package ir.appservice.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA Entity mapping for `permission`.
 */
@Entity
@Table(name = "permission")
public class PermissionJpaEntity implements Serializable {

    @Id
    @Column(name = "id", nullable = false, length = 255)
    private String id;

    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "module", length = 100)
    private String module;

    @Column(name = "registered_date")
    private Instant registeredDate = Instant.now();

    public PermissionJpaEntity() {
        this.id = UUID.randomUUID().toString();
    }

    public PermissionJpaEntity(String code, String description, String module) {
        this.id = UUID.randomUUID().toString();
        this.code = code;
        this.description = description;
        this.module = module;
        this.registeredDate = Instant.now();
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
        this.code = code;
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

    public Instant getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(Instant registeredDate) {
        this.registeredDate = registeredDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionJpaEntity that = (PermissionJpaEntity) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}
