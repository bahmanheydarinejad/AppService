package ir.appservice.api.rest.controller;

import ir.appservice.api.rest.service.GenericSearchService;
import ir.appservice.common.api.PagedApiResponse;
import ir.appservice.common.search.SearchRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Objects;

/**
 * Universal abstract base controller encapsulating uniform CRUD search endpoint logic,
 * request parameter validation, and paged envelope construction.
 *
 * @param <T>  Entity / DTO type
 * @param <ID> Entity identifier type
 */
public abstract class BaseSearchController<T, ID> {

    private final GenericSearchService<T, ID> searchService;

    protected BaseSearchController(GenericSearchService<T, ID> searchService) {
        this.searchService = Objects.requireNonNull(searchService, "searchService must not be null");
    }

    /**
     * @return Subject identifier for the entity (e.g. "users", "roles", "tenants")
     */
    public abstract String getSubject();

    /**
     * Executes dynamic search with filtering, sorting, and pagination.
     *
     * @param request search parameters
     * @return paged API response envelope
     */
    @PostMapping("/search")
    public ResponseEntity<PagedApiResponse<T>> search(@Valid @RequestBody(required = false) SearchRequest request) {
        long startTime = System.currentTimeMillis();

        if (request == null) {
            request = new SearchRequest(getSubject(), 0, 20);
        }
        if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
            request.setSubject(getSubject());
        }

        PagedApiResponse<T> response = searchService.search(request);
        if (response != null && response.getExecutionTimeMs() <= 0) {
            response.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        }

        return ResponseEntity.ok(response);
    }

    protected GenericSearchService<T, ID> getSearchService() {
        return searchService;
    }
}
