package ir.appservice_v1.model.service;

import ir.appservice_v1.model.entity.application.Role;
import ir.appservice_v1.model.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

@Service
@ApplicationScope
public class RoleService extends CrudService<Role> {

    public RoleService(RoleRepository roleRepository) {
        super(roleRepository, Role.class);
    }

}
