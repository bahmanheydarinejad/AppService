package ir.appservice_v1.model.converter;

import ir.appservice_v1.model.entity.application.Role;
import ir.appservice_v1.model.service.RoleService;
import org.springframework.stereotype.Component;

@Component
public class RoleConverter extends BaseConverter<Role> {

    public RoleConverter(RoleService roleService) {
        super(Role.class, roleService);
    }
}
