package ir.appservice_v1.view.beanComponents.panelBean.crudBean;

import ir.appservice_v1.model.entity.application.ui.Panel;
import ir.appservice_v1.model.service.PanelService;
import ir.appservice_v1.view.beanComponents.panelBean.BaseLazyCrudBean;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Setter
@Getter
@Component
@SessionScope
public class PanelCrudBean extends BaseLazyCrudBean<Panel> {

    public PanelCrudBean(PanelService panelService) {
        super(panelService, Panel.class);
    }
}
