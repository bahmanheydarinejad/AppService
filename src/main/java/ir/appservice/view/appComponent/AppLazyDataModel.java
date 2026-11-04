package ir.appservice.view.appComponent;

import ir.appservice.model.entity.BaseEntity;
import ir.appservice.model.service.CrudService;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.FilterMeta;
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

import java.util.*;

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
    public int count(Map<String, FilterMeta> filterBy) {
        return (int) this.crudService.pagedList(PageRequest.of(0, 1), extractFilters(filterBy)).getTotalElements();
    }

    private Map<String, Object> extractFilters(Map<String, FilterMeta> filterBy) {
        Map<String, Object> filters = new HashMap<>();
        if (filterBy != null) {
            filterBy.forEach((field, meta) -> {
                if (meta != null && meta.getFilterValue() != null) {
                    filters.put(field, meta.getFilterValue());
                }
            });
        }
        return filters;
    }

    @Override
    public List<T> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
        logger.trace("First: {}, PageSize: {}, SortMeta: {}, Filters: {}", first, pageSize, sortBy, filterBy);
        newInstance();

        List<Sort.Order> orders = new ArrayList<>();
        if (sortBy != null && !sortBy.isEmpty()) {
            for (SortMeta sortMeta : sortBy.values()) {
                String field = sortMeta.getField();
                if (field != null && !field.isEmpty()) {
                    if (sortMeta.getOrder() == SortOrder.DESCENDING) {
                        orders.add(JpaSort.JpaOrder.desc(field));
                    } else {
                        orders.add(JpaSort.JpaOrder.asc(field));
                    }
                }
            }
        }
        if (orders.isEmpty()) {
            orders.add(JpaSort.JpaOrder.asc("id"));
        }

        Map<String, Object> filters = extractFilters(filterBy);
        int pageIndex = pageSize > 0 ? first / pageSize : 0;
        Page<T> page = load(PageRequest.of(pageIndex, pageSize, Sort.by(orders)), filters);
        setWrappedData(page.getContent());
        this.setRowCount((int) page.getTotalElements());
        this.setPageSize(pageSize);

        logger.trace("AppLazyDataModel: Total:{} => {}", page.getTotalElements(), getWrappedData());
        return page.getContent();
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
    public String getRowKey(T object) {
        logger.trace("getRowKey: {}", object != null ? object.toString() : "null");
        return object != null ? object.getId() : null;
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

    public void delete() {
        remove();
    }

    public void restore() {
        recycle();
    }

    public void purge() {
        permanentRemove();
    }

    public void newInstance() {
        try {
            item = itemClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
