package ir.appservice_v1.view.beanComponents.panelBean.crudBean;

import ir.appservice_v1.model.entity.application.ResetPasswordToken;
import ir.appservice_v1.model.service.ResetPasswordTokenService;
import ir.appservice_v1.view.beanComponents.panelBean.BaseLazyCrudBean;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Setter
@Getter
@Component
@SessionScope
public class ResetPasswordTokenCrudBean extends BaseLazyCrudBean<ResetPasswordToken> {

    public ResetPasswordTokenCrudBean(ResetPasswordTokenService resetPasswordTokenService) {
        super(resetPasswordTokenService, ResetPasswordToken.class);
    }

}
