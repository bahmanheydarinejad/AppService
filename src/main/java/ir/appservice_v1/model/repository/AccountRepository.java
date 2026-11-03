package ir.appservice_v1.model.repository;

import ir.appservice_v1.model.entity.application.Account;
import org.springframework.stereotype.Repository;


@Repository
public interface AccountRepository extends CrudRepository<Account, String> {

    boolean existsByAccountNameIgnoreCase(String name);

    boolean existsByEmailIgnoreCase(String email);

    int countByAccountName(String name);

    Account findByAccountNameIgnoreCase(String name);

    Account findByMobileNumber(String phoneNumber);

    Account findByEmailIgnoreCase(String phoneNumber);

    boolean existsByAccountNameOrMobileNumberOrEmailAndPassword(String accountName, String mobileNumber, String email, String password);

}
