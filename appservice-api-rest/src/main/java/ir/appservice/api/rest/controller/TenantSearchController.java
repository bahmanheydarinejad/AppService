package ir.appservice.api.rest.controller;

import ir.appservice.api.rest.service.GenericSearchService;
import ir.appservice.api.rest.service.JpaSpecificationSearchService;
import ir.appservice.infrastructure.persistence.entity.TenantJpaEntity;
import ir.appservice.infrastructure.persistence.repository.SpringDataTenantRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Endpoint for Tenant dynamic search: POST /api/admin/tenants/search
 */
@RestController
@RequestMapping("/api/admin/tenants")
public class TenantSearchController extends BaseSearchController<TenantJpaEntity, String> {

    public TenantSearchController(SpringDataTenantRepository repository) {
        super(new JpaSpecificationSearchService<>(repository, "tenants"));
    }

    public TenantSearchController(GenericSearchService<TenantJpaEntity, String> searchService) {
        super(searchService);
    }

    @Override
    public String getSubject() {
        return "tenants";
    }
}
