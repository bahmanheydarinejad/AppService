package ir.appservice.api.rest;

import ir.appservice.api.rest.controller.BaseSearchController;
import ir.appservice.api.rest.service.GenericSearchService;
import ir.appservice.common.api.PagedApiResponse;
import ir.appservice.common.search.SearchRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BaseSearchControllerTest {

    static class DummyItem {
        private final String id;
        public DummyItem(String id) { this.id = id; }
        public String getId() { return id; }
    }

    static class DummySearchController extends BaseSearchController<DummyItem, String> {
        public DummySearchController(GenericSearchService<DummyItem, String> service) {
            super(service);
        }
        @Override
        public String getSubject() {
            return "dummy-items";
        }
    }

    @Test
    @DisplayName("STEP-7.1: Abstract BaseSearchController delegates to search service and wraps paged envelope")
    void testSearchDelegationAndEnvelope() {
        @SuppressWarnings("unchecked")
        GenericSearchService<DummyItem, String> searchService = mock(GenericSearchService.class);
        DummyItem item1 = new DummyItem("101");
        DummyItem item2 = new DummyItem("102");

        PagedApiResponse<DummyItem> mockResponse = PagedApiResponse.of("dummy-items", List.of(item1, item2), 0, 10, 2L, 5L);
        when(searchService.search(any(SearchRequest.class))).thenReturn(mockResponse);

        DummySearchController controller = new DummySearchController(searchService);

        SearchRequest request = new SearchRequest("dummy-items", 0, 10);
        ResponseEntity<PagedApiResponse<DummyItem>> responseEntity = controller.search(request);

        assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().isSuccess()).isTrue();
        assertThat(responseEntity.getBody().getSubject()).isEqualTo("dummy-items");
        assertThat(responseEntity.getBody().getData()).hasSize(2);
        assertThat(responseEntity.getBody().getTotalElements()).isEqualTo(2L);
        assertThat(responseEntity.getBody().getExecutionTimeMs()).isGreaterThan(0L);

        verify(searchService, times(1)).search(any(SearchRequest.class));
    }

    @Test
    @DisplayName("STEP-7.1: BaseSearchController defaults null request and missing subject correctly")
    void testSearchDefaultsWhenNullRequest() {
        @SuppressWarnings("unchecked")
        GenericSearchService<DummyItem, String> searchService = mock(GenericSearchService.class);
        when(searchService.search(any(SearchRequest.class)))
                .thenReturn(PagedApiResponse.of("dummy-items", List.of(), 0, 20, 0L, 0L));

        DummySearchController controller = new DummySearchController(searchService);

        ResponseEntity<PagedApiResponse<DummyItem>> response = controller.search(null);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSubject()).isEqualTo("dummy-items");
        assertThat(response.getBody().getPage()).isEqualTo(0);
        assertThat(response.getBody().getSize()).isEqualTo(20);
    }
}
