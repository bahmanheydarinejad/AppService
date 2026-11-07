package ir.appservice.api.rest.controller;

import ir.appservice.api.rest.service.GenericSearchService;
import ir.appservice.api.rest.service.JpaSpecificationSearchService;
import ir.appservice.infrastructure.persistence.entity.RoleJpaEntity;
import ir.appservice.infrastructure.persistence.repository.RoleJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Endpoint for Role dynamic search: POST /api/admin/roles/search
 */
@RestController
@RequestMapping("/api/admin/roles")
public class RoleSearchController extends BaseSearchController<RoleJpaEntity, String> {

    @Autowired
    public RoleSearchController(RoleJpaRepository repository) {
        super(new JpaSpecificationSearchService<>(repository, "roles"));
    }

    public RoleSearchController(GenericSearchService<RoleJpaEntity, String> searchService) {
        super(searchService);
    }

    @Override
    public String getSubject() {
        return "roles";
    }
}
