package ir.appservice.infrastructure.platform;

import ir.appservice.core.domain.entity.ScheduledJob;
import ir.appservice.core.domain.enums.Status;
import ir.appservice.infrastructure.platform.scheduler.PlatformJobSchedulerService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Quartz Job Scheduler & Thread Control Tests")
class PlatformJobSchedulerServiceTest {

    private static Scheduler scheduler;
    private static PlatformJobSchedulerService schedulerService;

    public static class SampleTestJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            // No-op execution for test verification
        }
    }

    @BeforeAll
    static void setUpAll() throws Exception {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        schedulerService = new PlatformJobSchedulerService(scheduler);
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown(true);
        }
    }

    @Test
    @DisplayName("Schedule, Inspect, Fire Now, Pause, Resume, and Delete job")
    void testSchedulerLifecycleAndControls() throws Exception {
        String jobName = "nightly_cleanup";
        String group = "maintenance";

        // 1. Schedule Job with Cron expression
        schedulerService.scheduleCronJob(
                jobName,
                group,
                SampleTestJob.class,
                "0 0 2 * * ?", // Every day at 2:00 AM
                "Purges expired sessions and soft-deleted temp files"
        );

        // 2. List jobs
        List<ScheduledJob> jobs = schedulerService.listJobs();
        assertThat(jobs).anyMatch(j -> j.getJobName().equals(jobName) && j.getJobGroup().equals(group));

        // 3. Fire Now
        schedulerService.fireNow(jobName, group);

        // 4. Pause Job
        schedulerService.pauseJob(jobName, group);
        List<ScheduledJob> pausedJobs = schedulerService.listJobs();
        ScheduledJob paused = pausedJobs.stream().filter(j -> j.getJobName().equals(jobName)).findFirst().orElseThrow();
        assertThat(paused.getStatus()).isEqualTo(Status.SUSPENDED);

        // 5. Resume Job
        schedulerService.resumeJob(jobName, group);
        List<ScheduledJob> resumedJobs = schedulerService.listJobs();
        ScheduledJob resumed = resumedJobs.stream().filter(j -> j.getJobName().equals(jobName)).findFirst().orElseThrow();
        assertThat(resumed.getStatus()).isEqualTo(Status.ACTIVE);

        // 6. Delete Job
        boolean deleted = schedulerService.deleteJob(jobName, group);
        assertThat(deleted).isTrue();
    }
}
