package org.springframework.boot.web.servlet.filter;

/**
 * Compatibility bridge for Spring Boot 4.x where OrderedFilter
 * was relocated to {@link org.springframework.boot.servlet.filter.OrderedFilter}.
 */
public interface OrderedFilter extends org.springframework.boot.servlet.filter.OrderedFilter {
}
