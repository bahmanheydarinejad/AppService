package ir.appservice_v1.model.repository;

import ir.appservice_v1.model.entity.application.Email;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends CrudRepository<Email, String> {

}
