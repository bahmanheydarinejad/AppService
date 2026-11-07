package ir.appservice.core.domain.entity;

import ir.appservice.core.domain.enums.Status;
import ir.appservice.core.domain.exception.DomainValidationException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Pure POJO Domain Entity: ScheduledJob.
 * Clustered job metadata and execution state tracker.
 * Zero Spring or JPA annotations. Standard JDK 17 only.
 */
public class ScheduledJob implements Serializable {

    private String id;
    private String jobName;
    private String jobGroup;
    private String cronExpression;
    private Status status;
    private Instant lastExecutionTime;
    private Instant nextExecutionTime;
    private long executionCount;
    private String fireInstanceId;
    private String tenantId;
    private Instant createdAt;
    private Instant updatedAt;

    public ScheduledJob() {
        this.status = Status.ACTIVE;
        this.executionCount = 0L;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public ScheduledJob(String id, String jobName, String jobGroup, String cronExpression, String tenantId) {
        if (jobName == null || jobName.trim().isEmpty()) {
            throw new DomainValidationException("jobName", "Job name cannot be null or empty");
        }
        if (jobGroup == null || jobGroup.trim().isEmpty()) {
            throw new DomainValidationException("jobGroup", "Job group cannot be null or empty");
        }
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            throw new DomainValidationException("cronExpression", "Cron expression cannot be null or empty");
        }
        this.id = id;
        this.jobName = jobName.trim();
        this.jobGroup = jobGroup.trim();
        this.cronExpression = cronExpression.trim();
        this.tenantId = tenantId;
        this.status = Status.ACTIVE;
        this.executionCount = 0L;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void recordExecution(Instant executionTime, Instant nextTime, String fireInstanceId) {
        this.lastExecutionTime = executionTime;
        this.nextExecutionTime = nextTime;
        this.fireInstanceId = fireInstanceId;
        this.executionCount++;
        this.updatedAt = Instant.now();
    }

    public void pause() {
        setStatus(Status.SUSPENDED);
    }

    public void resume() {
        setStatus(Status.ACTIVE);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        if (jobName == null || jobName.trim().isEmpty()) {
            throw new DomainValidationException("jobName", "Job name cannot be null or empty");
        }
        this.jobName = jobName.trim();
        this.updatedAt = Instant.now();
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        if (jobGroup == null || jobGroup.trim().isEmpty()) {
            throw new DomainValidationException("jobGroup", "Job group cannot be null or empty");
        }
        this.jobGroup = jobGroup.trim();
        this.updatedAt = Instant.now();
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            throw new DomainValidationException("cronExpression", "Cron expression cannot be null or empty");
        }
        this.cronExpression = cronExpression.trim();
        this.updatedAt = Instant.now();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status != null && this.status != null && !this.status.canTransitionTo(status)) {
            throw new DomainValidationException("status",
                    String.format("Invalid scheduled job status transition from %s to %s", this.status, status));
        }
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public Instant getLastExecutionTime() {
        return lastExecutionTime;
    }

    public void setLastExecutionTime(Instant lastExecutionTime) {
        this.lastExecutionTime = lastExecutionTime;
    }

    public Instant getNextExecutionTime() {
        return nextExecutionTime;
    }

    public void setNextExecutionTime(Instant nextExecutionTime) {
        this.nextExecutionTime = nextExecutionTime;
    }

    public long getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(long executionCount) {
        this.executionCount = executionCount;
    }

    public String getFireInstanceId() {
        return fireInstanceId;
    }

    public void setFireInstanceId(String fireInstanceId) {
        this.fireInstanceId = fireInstanceId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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
        ScheduledJob that = (ScheduledJob) o;
        return Objects.equals(jobName, that.jobName) &&
                Objects.equals(jobGroup, that.jobGroup) &&
                Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobName, jobGroup, tenantId);
    }

    @Override
    public String toString() {
        return "ScheduledJob{" +
                "id='" + id + '\'' +
                ", jobName='" + jobName + '\'' +
                ", jobGroup='" + jobGroup + '\'' +
                ", cronExpression='" + cronExpression + '\'' +
                ", status=" + status +
                ", executionCount=" + executionCount +
                '}';
    }
}
