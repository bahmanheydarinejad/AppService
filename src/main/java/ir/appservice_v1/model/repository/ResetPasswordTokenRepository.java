package ir.appservice_v1.model.repository;

import ir.appservice_v1.model.entity.application.ResetPasswordToken;
import org.springframework.stereotype.Repository;

@Repository
public interface ResetPasswordTokenRepository extends CrudRepository<ResetPasswordToken, String> {

    ResetPasswordToken findByToken(String token);

    boolean existsByToken(String token);

    int countByToken(String token);

}
