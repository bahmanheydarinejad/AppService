package ir.appservice_v1.view.beanComponents;

import ir.appservice_v1.configuration.exception.InvalidEmailException;
import ir.appservice_v1.configuration.exception.NotFoundEntityException;
import ir.appservice_v1.model.service.AccountService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.faces.context.FacesContext;

@Setter
@Getter
@Component
@RequestScope
public class AuthenticationBean extends BaseBean {

    private String loginId = "";
    private String password = "";
    private AccountService accountService;

    public AuthenticationBean(AccountService accountService) {
        this.accountService = accountService;
    }

    public void authenticate() {

        try {
            accountService.authenticate(loginId, password);
            logger.trace(String.format("Authenticated %s and redirecting ...", loginId));
            info("Welcome :)", "Welcome to home...", null);
            FacesContext.getCurrentInstance().getExternalContext().redirect("/dashboard");
        } catch (AuthenticationException bce) {
            error("You are not authenticated!", bce.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            fatal("Server Error!", e.getMessage(), null);
        }
    }

    public void restPasswordSendMail() {

        try {
            accountService.resetPasswordSendMail(loginId);
            info("Done", "Reset password link sent to mail account: " + loginId, null);
        } catch (NotFoundEntityException | InvalidEmailException e) {
            warn("UnSuccessful", e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            fatal("Server Error", "If this error persisted, please contact administrator.", null);
        }
    }


}