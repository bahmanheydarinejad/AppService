package ir.appservice_v1.view.appComponent;

import ir.appservice_v1.model.entity.BaseEntity;
import ir.appservice_v1.model.service.CrudService;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class AppLazyDataModel<T extends BaseEntity> extends LazyDataModel<T> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected List<T> filteredItems;
    protected Class<T> itemClass;
    protected List<Integer> rowsPerPage;
    protected T item;

    protected CrudService<T> crudService;

    public AppLazyDataModel(CrudService<T> crudService, Class<T> itemClass) {
        this.crudService = crudService;
        this.itemClass = itemClass;
        this.rowsPerPage = Arrays.asList(2, 5, 10, 30, 50, 100, 1000);
        this.setPageSize(rowsPerPage.get(1));
    }

    public String getRowsPerPageTemplate() {
        return this.rowsPerPage.toString().replace("[", "").replace("]", "").replace(" ", "");
    }

    @Override
    public List<T> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        logger.trace("First: {}, PageSize: {}, SortMeta: {}, Filters: {}", first, pageSize, multiSortMeta, filters);
        newInstance();

        List<Sort.Order> orders = new ArrayList<>();
        if (multiSortMeta != null) {
            multiSortMeta.stream().forEach(
                    sortMeta -> {
                        switch (sortMeta.getSortOrder()) {
                            case ASCENDING:
                                orders.add(JpaSort.JpaOrder.asc(sortMeta.getSortField()));
                                break;
                            case DESCENDING:
                                orders.add(JpaSort.JpaOrder.desc(sortMeta.getSortField()));
                                break;
                        }
                    }
            );
        }

        Page<T> page = load(PageRequest.of(first / pageSize, pageSize, Sort.by(orders)), filters);
        setWrappedData(page.getContent());
        this.setRowCount((int) page.getTotalElements());
        this.setPageSize(pageSize);

        logger.trace("Multi Sort - AppLazyDataModel: Total:{} => {}", page.getNumberOfElements(), getWrappedData());
        return getWrappedData();
    }

    @Override
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        logger.trace("First: {}, PageSize: {}, Sort Field: {}, SortOrder: {}, Filters: {}",
                first,
                pageSize,
                sortField,
                sortOrder,
                filters
        );
        newInstance();
        Sort sort;

        if (sortField == null || sortField.isEmpty()) {
            sortField = "id";
        }

        switch (sortOrder) {
            case ASCENDING:
                sort = JpaSort.by(Sort.Direction.ASC, sortField);
                break;
            case DESCENDING:
                sort = JpaSort.by(Sort.Direction.DESC, sortField);
                break;
            default:
                sort = JpaSort.by(sortField);
        }

        Page<T> page = load(PageRequest.of(first / pageSize, pageSize, sort), filters);
        setWrappedData(page.getContent());
        this.setRowCount((int) page.getTotalElements());
        this.setPageSize(pageSize);
        logger.trace("AppLazyDataModel: Total:{} => {}", page.getTotalElements(), getWrappedData());
        return getWrappedData();
    }

    protected Page<T> load(Pageable pageable, Map<String, Object> filters) {
        return this.crudService.pagedList(pageable, filters);
    }

    @Override
    public T getRowData(String rowKey) {
        logger.trace("getRowData: {}", rowKey);
        item = this.crudService.get(rowKey);
        return item;
    }

    @Override
    public Object getRowKey(T object) {
        logger.trace("getRowKey: {}", object.toString());
        return object.getId();
    }

    public void add() {
        logger.trace("Adding : {}", item);
        item = crudService.add(item);
        logger.trace("Added: {}", item);
    }

    public void edit() {
        logger.trace("Editing {}", item);
        item = crudService.edit(item);
        logger.trace("Edited: {}", item);
    }

    public void remove() {
        item = crudService.remove(item.getId());
        logger.trace("Removed: {}", item);
    }

    public void permanentRemove() {
        item = crudService.permanentRemove(item.getId());
        logger.trace("Permanent Removed: {}", item);
    }

    public void recycle() {
        item = crudService.recycle(item.getId());
        logger.trace("Recycled: {}", item);
    }

    public void activate() {
        item = crudService.activate(item.getId());
        logger.trace("Activated: {}", item);
    }

    public void deActivate() {
        item = crudService.deActivate(item.getId());
        logger.trace("DeActivated: {}", item);
    }

    public void newInstance() {
        try {
            item = itemClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        logger.trace("New Instance: {}", item);
    }

}
