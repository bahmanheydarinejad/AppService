package ir.appservice.core.domain.port.inbound;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Inbound Port (Primary / Driving Use Case): Generic Entity Search Contract.
 * Pure Java interface defining application boundary for dynamic search and pagination.
 */
public interface SearchEntityUseCase {

    /**
     * Executes dynamic search and returns a paged result set.
     *
     * @param query search criteria and pagination options
     * @param entityType domain entity class to search
     * @param <T> entity type parameter
     * @return paged search result
     */
    <T> SearchResult<T> search(SearchQuery query, Class<T> entityType);

    /**
     * Search query definition record.
     */
    record SearchQuery(
            String subject,
            int page,
            int size,
            String sortField,
            String sortDirection,
            String tenantId,
            Map<String, Object> filters
    ) {
        public SearchQuery {
            if (page < 0) page = 0;
            if (size <= 0) size = 20;
            if (sortField == null || sortField.trim().isEmpty()) sortField = "id";
            if (sortDirection == null || (!sortDirection.equalsIgnoreCase("ASC") && !sortDirection.equalsIgnoreCase("DESC"))) {
                sortDirection = "ASC";
            }
            if (tenantId == null || tenantId.trim().isEmpty()) tenantId = "default";
            if (filters == null) filters = Collections.emptyMap();
        }

        public static SearchQuery of(String subject, int page, int size) {
            return new SearchQuery(subject, page, size, "id", "ASC", "default", Collections.emptyMap());
        }
    }

    /**
     * Paged search result container record.
     */
    record SearchResult<T>(
            List<T> items,
            long totalElements,
            int page,
            int size,
            int totalPages
    ) {
        public SearchResult {
            if (items == null) items = Collections.emptyList();
        }

        public static <T> SearchResult<T> empty(int page, int size) {
            return new SearchResult<>(Collections.emptyList(), 0L, page, size, 0);
        }

        public static <T> SearchResult<T> of(List<T> items, long totalElements, int page, int size) {
            int calculatedTotalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
            return new SearchResult<>(items, totalElements, page, size, calculatedTotalPages);
        }
    }
}
