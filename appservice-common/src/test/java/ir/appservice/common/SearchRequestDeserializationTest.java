package ir.appservice.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.appservice.common.search.FilterCriterion;
import ir.appservice.common.search.FilterOperator;
import ir.appservice.common.search.LogicalOperator;
import ir.appservice.common.search.SearchRequest;
import ir.appservice.common.search.SortCriterion;
import ir.appservice.common.search.SortDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Search Request JSON Serialization & Deserialization Tests")
class SearchRequestDeserializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("SearchRequest: verify deserialization of complex nested filter criteria and sort rules")
    void testSearchRequestDeserialization() throws Exception {
        String json = """
                {
                  "subject": "accounts",
                  "page": 2,
                  "size": 15,
                  "conjunction": "AND",
                  "filters": [
                    {
                      "field": "username",
                      "operator": "CONTAINS",
                      "value": "admin"
                    },
                    {
                      "field": "registeredDate",
                      "operator": "BETWEEN",
                      "value": "2026-01-01T00:00:00Z",
                      "secondValue": "2026-12-31T23:59:59Z"
                    },
                    {
                      "field": "status",
                      "operator": "IN",
                      "values": ["ACTIVE", "PENDING"]
                    },
                    {
                      "field": "deleteDate",
                      "operator": "IS_NULL"
                    }
                  ],
                  "sorts": [
                    {
                      "field": "registeredDate",
                      "direction": "DESC"
                    },
                    {
                      "field": "username",
                      "direction": "ASC"
                    }
                  ]
                }
                """;

        SearchRequest request = objectMapper.readValue(json, SearchRequest.class);

        assertThat(request.getSubject()).isEqualTo("accounts");
        assertThat(request.getPage()).isEqualTo(2);
        assertThat(request.getSize()).isEqualTo(15);
        assertThat(request.getConjunction()).isEqualTo(LogicalOperator.AND);
        assertThat(request.getFilters()).hasSize(4);

        FilterCriterion f0 = request.getFilters().get(0);
        assertThat(f0.getField()).isEqualTo("username");
        assertThat(f0.getOperator()).isEqualTo(FilterOperator.CONTAINS);
        assertThat(f0.getValue()).isEqualTo("admin");

        FilterCriterion f1 = request.getFilters().get(1);
        assertThat(f1.getField()).isEqualTo("registeredDate");
        assertThat(f1.getOperator()).isEqualTo(FilterOperator.BETWEEN);
        assertThat(f1.getValue()).isEqualTo("2026-01-01T00:00:00Z");
        assertThat(f1.getSecondValue()).isEqualTo("2026-12-31T23:59:59Z");

        FilterCriterion f2 = request.getFilters().get(2);
        assertThat(f2.getField()).isEqualTo("status");
        assertThat(f2.getOperator()).isEqualTo(FilterOperator.IN);
        assertThat(f2.getValues()).containsExactly("ACTIVE", "PENDING");

        FilterCriterion f3 = request.getFilters().get(3);
        assertThat(f3.getField()).isEqualTo("deleteDate");
        assertThat(f3.getOperator()).isEqualTo(FilterOperator.IS_NULL);

        assertThat(request.getSorts()).hasSize(2);
        assertThat(request.getSorts().get(0).getField()).isEqualTo("registeredDate");
        assertThat(request.getSorts().get(0).getDirection()).isEqualTo(SortDirection.DESC);
        assertThat(request.getSorts().get(1).getField()).isEqualTo("username");
        assertThat(request.getSorts().get(1).getDirection()).isEqualTo(SortDirection.ASC);
    }

    @Test
    @DisplayName("SearchRequest: verify fluent builder helper methods")
    void testSearchRequestBuilderMethods() {
        SearchRequest req = new SearchRequest("roles", 0, 10)
                .addFilter(FilterCriterion.equals("name", "ROLE_ADMIN"))
                .addFilter(FilterCriterion.isNotNull("description"))
                .addSort(SortCriterion.desc("createdAt"));

        assertThat(req.getFilters()).hasSize(2);
        assertThat(req.getSorts()).hasSize(1);
        assertThat(req.getSorts().get(0).isAscending()).isFalse();
    }
}
