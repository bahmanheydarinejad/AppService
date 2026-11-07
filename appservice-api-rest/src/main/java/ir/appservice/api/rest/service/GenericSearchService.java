package ir.appservice.api.rest.service;

import ir.appservice.common.api.PagedApiResponse;
import ir.appservice.common.search.SearchRequest;

/**
 * Service contract for generic entity search and pagination.
 *
 * @param <T>  Entity / DTO type
 * @param <ID> Entity identifier type
 */
public interface GenericSearchService<T, ID> {

    /**
     * Executes dynamic search and returns a standardized paged envelope.
     *
     * @param request search filters, sorts, pagination
     * @return standardized PagedApiResponse
     */
    PagedApiResponse<T> search(SearchRequest request);
}
