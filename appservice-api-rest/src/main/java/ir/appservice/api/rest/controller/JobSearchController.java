package ir.appservice.api.rest.controller;

import ir.appservice.api.rest.service.GenericSearchService;
import ir.appservice.common.api.PagedApiResponse;
import ir.appservice.core.domain.entity.ScheduledJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * REST Endpoint for Scheduled Quartz Job dynamic search: POST /api/admin/jobs/search
 */
@RestController
@RequestMapping("/api/admin/jobs")
public class JobSearchController extends BaseSearchController<ScheduledJob, String> {

    @Autowired(required = false)
    public JobSearchController() {
        this(request -> PagedApiResponse.of("jobs", Collections.emptyList(), request.getPage(), request.getSize(), 0L, 1L));
    }

    public JobSearchController(GenericSearchService<ScheduledJob, String> searchService) {
        super(searchService);
    }

    @Override
    public String getSubject() {
        return "jobs";
    }
}
