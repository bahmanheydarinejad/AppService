package ir.appservice_v1.model.converter;

import ir.appservice_v1.model.entity.application.Account;
import ir.appservice_v1.model.service.AccountService;
import org.springframework.stereotype.Component;

@Component
public class AccountConverter extends BaseConverter<Account> {

    public AccountConverter(AccountService accountService) {
        super(Account.class, accountService);
    }
}
