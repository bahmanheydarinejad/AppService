package ir.appservice_v1.view.beanComponents.panelBean;

import ir.appservice_v1.model.entity.BaseEntity;
import ir.appservice_v1.model.service.CrudService;
import ir.appservice_v1.view.appComponent.AppLazyDataModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseLazyCrudBean<T extends BaseEntity> extends BasePanelBean {

    protected AppLazyDataModel<T> appLazyDataModel;
    protected Class<T> itemClass;
    protected CrudService<T> crudService;

    public BaseLazyCrudBean(CrudService<T> crudService, Class<T> itemClass) {
        this.crudService = crudService;
        this.itemClass = itemClass;
        this.appLazyDataModel = new AppLazyDataModel<T>(crudService, itemClass) {
        };
    }

    public String rowClass(T item) {
        try {
            if (item.getDeleteDate() != null) {
                return "alert alert-danger text-black";
            } else if (item.getStatue().equals(T.DE_ACTIVE_STATUS)) {
                return "alert alert-warning text-black";
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return "";
        }
    }

}
