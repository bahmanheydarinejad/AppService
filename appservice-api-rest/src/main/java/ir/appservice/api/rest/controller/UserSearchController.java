package ir.appservice.api.rest.controller;

import ir.appservice.api.rest.service.GenericSearchService;
import ir.appservice.api.rest.service.JpaSpecificationSearchService;
import ir.appservice.infrastructure.persistence.entity.UserJpaEntity;
import ir.appservice.infrastructure.persistence.repository.SpringDataUserRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Endpoint for User dynamic search: POST /api/admin/users/search
 */
@RestController
@RequestMapping("/api/admin/users")
public class UserSearchController extends BaseSearchController<UserJpaEntity, String> {

    public UserSearchController(SpringDataUserRepository repository) {
        super(new JpaSpecificationSearchService<>(repository, "users"));
    }

    public UserSearchController(GenericSearchService<UserJpaEntity, String> searchService) {
        super(searchService);
    }

    @Override
    public String getSubject() {
        return "users";
    }
}
