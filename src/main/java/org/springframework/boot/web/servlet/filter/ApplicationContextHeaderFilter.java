package org.springframework.boot.web.servlet.filter;

import org.springframework.context.ApplicationContext;

public class ApplicationContextHeaderFilter extends org.springframework.boot.servlet.filter.ApplicationContextHeaderFilter implements OrderedFilter {
    private int order = OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER - 100;

    public ApplicationContextHeaderFilter(ApplicationContext context) {
        super(context);
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
