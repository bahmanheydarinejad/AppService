package ir.appservice_v1.model.repository;

import ir.appservice_v1.model.entity.application.Role;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends CrudRepository<Role, String> {


}
