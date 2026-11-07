package ir.appservice.api.rest.controller;

import ir.appservice.common.api.ApiResponse;
import ir.appservice.core.domain.entity.ScheduledJob;
import ir.appservice.infrastructure.platform.scheduler.PlatformJobSchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Endpoint for Quartz Job Scheduler Management: list, fire now, pause, and resume.
 */
@RestController
@RequestMapping("/api/admin/jobs")
public class SchedulerManagementController {

    private final PlatformJobSchedulerService schedulerService;

    public SchedulerManagementController(PlatformJobSchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('JOBS_READ')")
    public ResponseEntity<ApiResponse<List<ScheduledJob>>> listJobs() throws Exception {
        List<ScheduledJob> jobs = schedulerService.listJobs();
        return ResponseEntity.ok(ApiResponse.success(jobs, "Jobs listed successfully"));
    }

    @PostMapping("/{id}/fire")
    @PreAuthorize("hasAuthority('JOBS_EXECUTE')")
    public ResponseEntity<ApiResponse<Void>> fireJob(@PathVariable("id") String id) throws Exception {
        String[] parts = parseJobId(id);
        schedulerService.fireNow(parts[1], parts[0]);
        return ResponseEntity.ok(ApiResponse.success(null, "Job triggered successfully: " + id));
    }

    @PostMapping("/{id}/pause")
    @PreAuthorize("hasAuthority('JOBS_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> pauseJob(@PathVariable("id") String id) throws Exception {
        String[] parts = parseJobId(id);
        schedulerService.pauseJob(parts[1], parts[0]);
        return ResponseEntity.ok(ApiResponse.success(null, "Job paused successfully: " + id));
    }

    @PostMapping("/{id}/resume")
    @PreAuthorize("hasAuthority('JOBS_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> resumeJob(@PathVariable("id") String id) throws Exception {
        String[] parts = parseJobId(id);
        schedulerService.resumeJob(parts[1], parts[0]);
        return ResponseEntity.ok(ApiResponse.success(null, "Job resumed successfully: " + id));
    }

    private String[] parseJobId(String id) {
        if (id != null && id.contains(":")) {
            String[] split = id.split(":", 2);
            return new String[]{split[0], split[1]};
        }
        return new String[]{"DEFAULT", id};
    }
}
