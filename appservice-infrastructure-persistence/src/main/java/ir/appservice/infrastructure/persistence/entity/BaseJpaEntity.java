package ir.appservice.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Base JPA MappedSuperclass enforcing identity, audit timestamps, and soft-delete invariants.
 */
@MappedSuperclass
public abstract class BaseJpaEntity implements Serializable {

    @Id
    @Column(name = "id", nullable = false, length = 255)
    private String id;

    @Column(name = "statue", length = 255)
    private String status = "ACTIVE_STATUS";

    @Column(name = "registered_date")
    private Instant registeredDate;

    @Column(name = "updated_date")
    private Instant updatedDate;

    @Column(name = "delete_date")
    private Instant deleteDate;

    public BaseJpaEntity() {
        this.id = UUID.randomUUID().toString();
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
    }

    public void softDelete() {
        this.deleteDate = Instant.now();
        this.status = "DE_ACTIVE_STATUS";
    }

    public boolean isDeleted() {
        return this.deleteDate != null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Instant getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Instant deleteDate) {
        this.deleteDate = deleteDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseJpaEntity that = (BaseJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
