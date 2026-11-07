package ir.appservice.api.rest.controller;

import ir.appservice.api.rest.service.GenericSearchService;
import ir.appservice.api.rest.service.JpaSpecificationSearchService;
import ir.appservice.infrastructure.persistence.entity.AuditEventJpaEntity;
import ir.appservice.infrastructure.persistence.repository.SpringDataAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Endpoint for AuditEvent dynamic search: POST /api/admin/audit-events/search
 */
@RestController
@RequestMapping("/api/admin/audit-events")
public class AuditEventSearchController extends BaseSearchController<AuditEventJpaEntity, String> {

    @Autowired
    public AuditEventSearchController(SpringDataAuditLogRepository repository) {
        super(new JpaSpecificationSearchService<>(repository, "audit-events"));
    }

    public AuditEventSearchController(GenericSearchService<AuditEventJpaEntity, String> searchService) {
        super(searchService);
    }

    @Override
    public String getSubject() {
        return "audit-events";
    }
}
