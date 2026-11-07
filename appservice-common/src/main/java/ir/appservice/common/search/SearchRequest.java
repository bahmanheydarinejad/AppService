package ir.appservice.common.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Universal CRUD search request body model.
 * Sent by clients to execute dynamic filtering, pagination, and multi-column sorting.
 */
public class SearchRequest implements Serializable {

    private String subject;
    private int page = 0;
    private int size = 20;
    private List<FilterCriterion> filters = new ArrayList<>();
    private List<SortCriterion> sorts = new ArrayList<>();
    private LogicalOperator conjunction = LogicalOperator.AND;

    public SearchRequest() {
    }

    public SearchRequest(String subject, int page, int size) {
        this.subject = subject;
        this.page = Math.max(0, page);
        this.size = size > 0 ? size : 20;
    }

    public SearchRequest addFilter(FilterCriterion filter) {
        if (filter != null) {
            this.filters.add(filter);
        }
        return this;
    }

    public SearchRequest addSort(SortCriterion sort) {
        if (sort != null) {
            this.sorts.add(sort);
        }
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size > 0 ? size : 20;
    }

    public List<FilterCriterion> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterCriterion> filters) {
        this.filters = filters != null ? filters : new ArrayList<>();
    }

    public List<SortCriterion> getSorts() {
        return sorts;
    }

    public void setSorts(List<SortCriterion> sorts) {
        this.sorts = sorts != null ? sorts : new ArrayList<>();
    }

    public LogicalOperator getConjunction() {
        return conjunction;
    }

    public void setConjunction(LogicalOperator conjunction) {
        this.conjunction = conjunction != null ? conjunction : LogicalOperator.AND;
    }
}
