package ir.appservice_v1.model.converter;

import ir.appservice_v1.model.entity.application.ui.Panel;
import ir.appservice_v1.model.service.PanelService;
import org.springframework.stereotype.Component;

@Component
public class PanelConverter extends BaseConverter<Panel> {

    public PanelConverter(PanelService panelService) {
        super(Panel.class, panelService);
    }
}
