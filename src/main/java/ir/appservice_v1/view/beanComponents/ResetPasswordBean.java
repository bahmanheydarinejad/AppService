package ir.appservice_v1.view.beanComponents;

import ir.appservice_v1.model.service.ResetPasswordTokenService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.faces.context.FacesContext;

@Setter
@Getter
@Component
@RequestScope
public class ResetPasswordBean extends BaseBean {

    private boolean tokenValid;
    private int tokenCount;
    private String token;
    private String newPassword;
    private ResetPasswordTokenService resetPasswordTokenService;

    public ResetPasswordBean(ResetPasswordTokenService resetPasswordTokenService) {
        this.resetPasswordTokenService = resetPasswordTokenService;
        this.token =
                FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(
                        "token");
    }

    public boolean isTokenValid() {
        return resetPasswordTokenService.isExistByToken(token);
    }

    public void resetPassword() {
        try {
            resetPasswordTokenService.resetPassword(token, newPassword);
            info("Success", "Password Reset Successfully", null);
            FacesContext.getCurrentInstance().getExternalContext().redirect("/index");
        } catch (Exception e) {
            error("Error", e.getMessage(), null);
        }
    }

}