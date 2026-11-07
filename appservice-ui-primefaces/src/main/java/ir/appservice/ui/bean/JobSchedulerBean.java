package ir.appservice.ui.bean;

import ir.appservice.core.domain.entity.ScheduledJob;
import ir.appservice.infrastructure.platform.scheduler.PlatformJobSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean for Panel 2: Job Scheduler Management Panel.
 */
@Component("jobSchedulerBean")
@SessionScope
public class JobSchedulerBean implements Serializable {

    private final PlatformJobSchedulerService schedulerService;

    private List<ScheduledJob> jobs = new ArrayList<>();
    private ScheduledJob selectedJob;
    private String statusMessage;

    @Autowired
    public JobSchedulerBean(PlatformJobSchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    public void loadJobs() {
        if (schedulerService != null) {
            try {
                this.jobs = schedulerService.listJobs();
            } catch (Exception e) {
                this.statusMessage = "Error loading jobs: " + e.getMessage();
            }
        }
    }

    public void fireNow(ScheduledJob job) {
        if (job != null && schedulerService != null) {
            try {
                schedulerService.fireNow(job.getJobName(), job.getJobGroup());
                this.statusMessage = "Job triggered: " + job.getJobName();
                loadJobs();
            } catch (Exception e) {
                this.statusMessage = "Failed to fire job: " + e.getMessage();
            }
        }
    }

    public void pauseJob(ScheduledJob job) {
        if (job != null && schedulerService != null) {
            try {
                schedulerService.pauseJob(job.getJobName(), job.getJobGroup());
                this.statusMessage = "Job paused: " + job.getJobName();
                loadJobs();
            } catch (Exception e) {
                this.statusMessage = "Failed to pause job: " + e.getMessage();
            }
        }
    }

    public void resumeJob(ScheduledJob job) {
        if (job != null && schedulerService != null) {
            try {
                schedulerService.resumeJob(job.getJobName(), job.getJobGroup());
                this.statusMessage = "Job resumed: " + job.getJobName();
                loadJobs();
            } catch (Exception e) {
                this.statusMessage = "Failed to resume job: " + e.getMessage();
            }
        }
    }

    // Getters and Setters
    public List<ScheduledJob> getJobs() { return jobs; }
    public void setJobs(List<ScheduledJob> jobs) { this.jobs = jobs; }

    public ScheduledJob getSelectedJob() { return selectedJob; }
    public void setSelectedJob(ScheduledJob selectedJob) { this.selectedJob = selectedJob; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
}
