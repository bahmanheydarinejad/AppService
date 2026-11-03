package ir.appservice_v1.model.service;

import ir.appservice_v1.configuration.exception.InvalidTokenException;
import ir.appservice_v1.model.entity.application.Account;
import ir.appservice_v1.model.entity.application.ResetPasswordToken;
import ir.appservice_v1.model.repository.ResetPasswordTokenRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

@Service
@ApplicationScope
public class ResetPasswordTokenService extends CrudService<ResetPasswordToken> {


    private AccountService accountService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public ResetPasswordTokenService(ResetPasswordTokenRepository resetPasswordTokenRepository,
                                     AccountService accountService,
                                     BCryptPasswordEncoder bCryptPasswordEncoder
    ) {
        super(resetPasswordTokenRepository, ResetPasswordToken.class);
        this.accountService = accountService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public ResetPasswordTokenRepository getResetPasswordTokenRepository() {
        return (ResetPasswordTokenRepository) this.crudRepository;
    }

    public boolean isExistByToken(String token) {
        return getResetPasswordTokenRepository().existsByToken(token);
    }

    public void resetPassword(String token, String newPassword) {
        int tokenCount = getResetPasswordTokenRepository().countByToken(token);
        logger.trace("resetPassword:{} => {}, New Password: {}", tokenCount, token, newPassword);
        if (tokenCount == 0) {
            throw new InvalidTokenException(token);
        } else if (tokenCount == 1) {
            ResetPasswordToken resetPasswordToken = getResetPasswordTokenRepository().findByToken(token);
            Account account = resetPasswordToken.getAccount();
            account.setPassword(bCryptPasswordEncoder.encode(newPassword));
            accountService.edit(account);
            permanentRemove(resetPasswordToken);
        } else {
            throw new InvalidTokenException(token);
        }
    }
}