package ir.appservice_v1.model.service;

import ir.appservice_v1.model.entity.application.ui.Menu;
import ir.appservice_v1.model.repository.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.List;

@Service
@ApplicationScope
public class MenuService extends CrudService<Menu> {

    public MenuService(MenuRepository menuRepository) {
        super(menuRepository, Menu.class);
    }

    public List<Menu> list(String type) {
        return getMenuRepository().findAllByTypeIgnoreCaseAndParentIsNullAndDeleteDateIsNull(type);
    }

    private MenuRepository getMenuRepository() {
        return ((MenuRepository) crudRepository);
    }

    public List<Menu> listWithoutPanel() {
        return getMenuRepository().findAllByTypeAndPanelIsNullAndDeleteDateIsNull(Menu.PANEL_MENU);
    }

    public List<Menu> listParents() {
        return getMenuRepository().findAllByTypeAndDeleteDateIsNull(Menu.CATEGORY_MENU);
    }

}