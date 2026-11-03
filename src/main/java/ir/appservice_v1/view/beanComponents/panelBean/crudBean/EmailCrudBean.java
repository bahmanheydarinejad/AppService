package ir.appservice_v1.view.beanComponents.panelBean.crudBean;

import ir.appservice_v1.model.entity.application.Email;
import ir.appservice_v1.model.service.EMailService;
import ir.appservice_v1.view.beanComponents.panelBean.BaseLazyCrudBean;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Setter
@Getter
@Component
@SessionScope
public class EmailCrudBean extends BaseLazyCrudBean<Email> {

    public EmailCrudBean(EMailService eMailService) {
        super(eMailService, Email.class);
    }
}
