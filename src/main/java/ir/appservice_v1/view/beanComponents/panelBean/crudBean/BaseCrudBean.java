package ir.appservice_v1.view.beanComponents.panelBean.crudBean;

import ir.appservice_v1.model.entity.BaseEntity;
import ir.appservice_v1.model.service.CrudService;
import ir.appservice_v1.view.beanComponents.panelBean.BasePanelBean;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public abstract class BaseCrudBean<T extends BaseEntity> extends BasePanelBean {

    protected CrudService<T> crudService;

    protected List<T> items;
    protected List<T> filteredItems;
    protected Class<T> clazz;
    protected T item;

    protected int rowsPerPage = 5;

    public BaseCrudBean(Class<T> clazz, CrudService<T> crudService) {
        logger.trace("Initializing");
        this.crudService = crudService;
        this.clazz = clazz;

        newInstance();
        list();
    }

    public void newInstance() {
        item = null;
        try {
            item = this.clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        logger.trace("Item: " + item);
    }

    public void add() {
        logger.trace("add");
        T temp = crudService.add(item);
        items.add(0, temp);
        newInstance();

        info(String.format("%s added", temp.getClass().getSimpleName()), String.format("%s %s was added.", temp.getClass().getSimpleName(), temp.getDisplayName()), null);
        logger.trace(String.format("%s saved: ID: %s, DisplayName: %s", temp.getClass().getSimpleName(), temp.getId(), temp.getDisplayName()));
    }

    public void edit() {
        logger.trace("edit");
        T temp = crudService.edit(item);
        items.remove(temp);
        items.add(0, temp);
        newInstance();

        logger.trace(String.format("%s saved: ID: %s, DisplayName: %s", temp.getClass().getSimpleName(), temp.getId(), temp.getDisplayName()));
        info(String.format("%s edited", temp.getClass().getSimpleName()), String.format("%s %s was edited.", temp.getClass().getSimpleName(), temp.getDisplayName()), null);
    }


    public void remove(String id) {
        logger.trace("remove");
        T temp = crudService.remove(id);
        items.remove(temp);

        logger.trace(String.format("%s removed: ID: %s, DisplayName: %s", temp.getClass().getSimpleName(), temp.getId(), temp.getDisplayName()));
        warn(String.format("%s removed", temp.getClass().getSimpleName()), String.format("%s %s was removed.", temp.getClass().getSimpleName(), temp.getDisplayName()), null);
    }

    public void load(String id) {
        logger.trace("load");
        item = crudService.get(id);
//        item = crudService.fullLazyGet(id);

        logger.trace(String.format("%s loaded: ID: %s, DisplayName: %s", item.getClass().getSimpleName(), item.getId(), item.getDisplayName()));
    }

    public void list() {
        logger.trace("list");
        items = crudService.list();
    }

}
