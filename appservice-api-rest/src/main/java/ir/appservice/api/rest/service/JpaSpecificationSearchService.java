package ir.appservice.api.rest.service;

import ir.appservice.common.api.PagedApiResponse;
import ir.appservice.common.search.SearchRequest;
import ir.appservice.common.search.SortCriterion;
import ir.appservice.common.search.SortDirection;
import ir.appservice.infrastructure.persistence.specification.GenericSpecificationBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable JPA Specification search service implementing GenericSearchService.
 *
 * @param <T> JPA Entity type
 */
public class JpaSpecificationSearchService<T> implements GenericSearchService<T, String> {

    private final JpaSpecificationExecutor<T> repository;
    private final String subject;

    public JpaSpecificationSearchService(JpaSpecificationExecutor<T> repository, String subject) {
        this.repository = repository;
        this.subject = subject;
    }

    @Override
    public PagedApiResponse<T> search(SearchRequest request) {
        long start = System.currentTimeMillis();
        int page = request != null ? request.getPage() : 0;
        int size = (request != null && request.getSize() > 0) ? request.getSize() : 20;

        List<Sort.Order> orders = new ArrayList<>();
        if (request != null && request.getSorts() != null) {
            for (SortCriterion s : request.getSorts()) {
                if (s.getField() != null && !s.getField().isBlank()) {
                    orders.add(s.getDirection() == SortDirection.DESC
                            ? Sort.Order.desc(s.getField())
                            : Sort.Order.asc(s.getField()));
                }
            }
        }
        Sort sort = orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<T> spec = GenericSpecificationBuilder.toSpecification(request);
        Page<T> resultPage = repository.findAll(spec, pageable);

        return PagedApiResponse.of(
                subject,
                resultPage.getContent(),
                page,
                size,
                resultPage.getTotalElements(),
                System.currentTimeMillis() - start
        );
    }
}
