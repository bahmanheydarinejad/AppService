package ir.appservice_v1.model.converter;

import ir.appservice_v1.model.entity.application.ui.Menu;
import ir.appservice_v1.model.service.MenuService;
import org.springframework.stereotype.Component;

@Component
public class MenuConverter extends BaseConverter<Menu> {

    public MenuConverter(MenuService menuService) {
        super(Menu.class, menuService);
    }
}
