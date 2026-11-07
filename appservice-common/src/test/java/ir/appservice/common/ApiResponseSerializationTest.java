package ir.appservice.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ir.appservice.common.api.ApiResponse;
import ir.appservice.common.api.PagedApiResponse;
import ir.appservice.common.context.TraceContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("API Response Envelopes JSON Serialization Tests")
class ApiResponseSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        TraceContextHolder.setTraceId("tr-test-fixed-123");
    }

    @AfterEach
    void tearDown() {
        TraceContextHolder.clear();
    }

    @Test
    @DisplayName("ApiResponse: verify JSON structure, traceId, and ISO-8601 timestamp format")
    void testApiResponseSerialization() throws Exception {
        ApiResponse<String> response = ApiResponse.success("sample_payload", "Fetched successfully");

        String json = objectMapper.writeValueAsString(response);
        JsonNode node = objectMapper.readTree(json);

        assertThat(node.get("success").asBoolean()).isTrue();
        assertThat(node.get("statusCode").asInt()).isEqualTo(200);
        assertThat(node.get("message").asText()).isEqualTo("Fetched successfully");
        assertThat(node.get("data").asText()).isEqualTo("sample_payload");
        assertThat(node.get("traceId").asText()).isEqualTo("tr-test-fixed-123");

        // Validate timestamp ISO-8601 formatting (e.g. 2026-11-07T...)
        String timestampStr = node.get("timestamp").asText();
        assertThat(timestampStr).matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$");
        assertThat(Instant.parse(timestampStr)).isNotNull();
    }

    @Test
    @DisplayName("ApiResponse: verify error envelope structure and messages list")
    void testApiResponseErrorSerialization() throws Exception {
        ApiResponse<Void> errorResponse = ApiResponse.error("Validation failed", 400, List.of("Field email is invalid", "Password too short"));

        String json = objectMapper.writeValueAsString(errorResponse);
        JsonNode node = objectMapper.readTree(json);

        assertThat(node.get("success").asBoolean()).isFalse();
        assertThat(node.get("statusCode").asInt()).isEqualTo(400);
        assertThat(node.get("errors")).hasSize(2);
        assertThat(node.get("errors").get(0).asText()).isEqualTo("Field email is invalid");
    }

    @Test
    @DisplayName("PagedApiResponse: verify paged pagination metadata and totalPages calculation")
    void testPagedApiResponseSerialization() throws Exception {
        List<String> items = List.of("User1", "User2", "User3");
        PagedApiResponse<String> paged = PagedApiResponse.of("users", items, 0, 20, 48L, 12L);

        String json = objectMapper.writeValueAsString(paged);
        JsonNode node = objectMapper.readTree(json);

        assertThat(node.get("success").asBoolean()).isTrue();
        assertThat(node.get("subject").asText()).isEqualTo("users");
        assertThat(node.get("page").asInt()).isEqualTo(0);
        assertThat(node.get("size").asInt()).isEqualTo(20);
        assertThat(node.get("totalElements").asLong()).isEqualTo(48L);
        assertThat(node.get("totalPages").asInt()).isEqualTo(3); // ceil(48 / 20) = 3
        assertThat(node.get("executionTimeMs").asLong()).isEqualTo(12L);
        assertThat(node.get("data")).hasSize(3);
    }
}
