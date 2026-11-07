package ir.appservice.api.rest.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controller exposing OpenAPI 3.0 specification at /v3/api-docs.
 */
@RestController
public class OpenApiController {

    @GetMapping(value = "/v3/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getOpenApiDocs() {
        Map<String, Object> openApi = Map.of(
                "openapi", "3.0.1",
                "info", Map.of(
                        "title", "AppService Enterprise Administration REST API",
                        "description", "Hexagonal Architecture REST API Plane with uniform CRUD search, multi-tenancy, and administrative management endpoints.",
                        "version", "1.0.0"
                ),
                "servers", List.of(
                        Map.of("url", "http://localhost:8080", "description", "Local Development Server")
                ),
                "paths", Map.of(
                        "/api/admin/users/search", Map.of("post", Map.of("summary", "Dynamic Search Users", "tags", List.of("Users"))),
                        "/api/admin/roles/search", Map.of("post", Map.of("summary", "Dynamic Search Roles", "tags", List.of("Roles"))),
                        "/api/admin/tenants/search", Map.of("post", Map.of("summary", "Dynamic Search Tenants", "tags", List.of("Tenants"))),
                        "/api/admin/audit-events/search", Map.of("post", Map.of("summary", "Dynamic Search Audit Events", "tags", List.of("Audit"))),
                        "/api/admin/sms/search", Map.of("post", Map.of("summary", "Dynamic Search SMS Logs", "tags", List.of("SMS"))),
                        "/api/admin/jobs/search", Map.of("post", Map.of("summary", "Dynamic Search Scheduled Jobs", "tags", List.of("Scheduler")))
                )
        );
        return ResponseEntity.ok(openApi);
    }
}
