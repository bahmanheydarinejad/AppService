package ir.appservice.api.rest.controller;

import ir.appservice.api.rest.service.GenericSearchService;
import ir.appservice.common.api.PagedApiResponse;
import ir.appservice.common.search.SearchRequest;
import ir.appservice.core.domain.entity.SmsMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * REST Endpoint for Outbound SMS Log dynamic search: POST /api/admin/sms/search
 */
@RestController
@RequestMapping("/api/admin/sms")
public class SmsSearchController extends BaseSearchController<SmsMessage, String> {

    @Autowired(required = false)
    public SmsSearchController() {
        this(request -> PagedApiResponse.of("sms", Collections.emptyList(), request.getPage(), request.getSize(), 0L, 1L));
    }

    public SmsSearchController(GenericSearchService<SmsMessage, String> searchService) {
        super(searchService);
    }

    @Override
    public String getSubject() {
        return "sms";
    }
}
