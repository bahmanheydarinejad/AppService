package ir.appservice.infrastructure.platform.scheduler;

import ir.appservice.core.domain.entity.ScheduledJob;
import ir.appservice.core.domain.enums.Status;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Clustered Quartz Scheduler Service with manual thread controls ("Fire Now", pause, resume, abort).
 */
@Service
public class PlatformJobSchedulerService {

    private final Scheduler scheduler;

    public PlatformJobSchedulerService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Schedules a recurring job using a standard Cron expression.
     */
    public void scheduleCronJob(String jobName, String jobGroup, Class<? extends Job> jobClass, String cronExpression, String description)
            throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobKey)
                .withDescription(description)
                .storeDurably()
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "_trigger", jobGroup)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }

        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * Immediate thread trigger ("Fire Now").
     */
    public void fireNow(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        scheduler.triggerJob(jobKey);
    }

    /**
     * Pauses all triggers associated with the job.
     */
    public void pauseJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        scheduler.pauseJob(jobKey);
    }

    /**
     * Resumes execution of the paused job.
     */
    public void resumeJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        scheduler.resumeJob(jobKey);
    }

    /**
     * Deletes the job from the Quartz registry.
     */
    public boolean deleteJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        return scheduler.deleteJob(jobKey);
    }

    /**
     * Inspects active scheduled jobs across cluster nodes.
     */
    public List<ScheduledJob> listJobs() throws SchedulerException {
        List<ScheduledJob> result = new ArrayList<>();
        if (scheduler == null) return result;

        for (String group : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
                JobDetail detail = scheduler.getJobDetail(jobKey);
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);

                ScheduledJob job = new ScheduledJob();
                job.setId(jobKey.getName());
                job.setJobName(jobKey.getName());
                job.setJobGroup(jobKey.getGroup());
                job.setStatus(Status.ACTIVE);

                String cronExpr = "0 0 * * * ?";
                if (!triggers.isEmpty()) {
                    Trigger trigger = triggers.get(0);
                    if (trigger instanceof org.quartz.CronTrigger cronTrigger) {
                        cronExpr = cronTrigger.getCronExpression();
                    }
                    Date nextFire = trigger.getNextFireTime();
                    Date prevFire = trigger.getPreviousFireTime();
                    if (nextFire != null) job.setNextExecutionTime(nextFire.toInstant());
                    if (prevFire != null) job.setLastExecutionTime(prevFire.toInstant());

                    Trigger.TriggerState state = scheduler.getTriggerState(trigger.getKey());
                    if (state == Trigger.TriggerState.PAUSED) {
                        job.setStatus(Status.SUSPENDED);
                    }
                }
                job.setCronExpression(cronExpr);

                result.add(job);
            }
        }
        return result;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}
