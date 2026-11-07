package ir.appservice.api.rest;

import ir.appservice.api.rest.controller.OpenApiController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OpenApiControllerTest {

    @Test
    @DisplayName("STEP-7.4: OpenApiController exposes OpenAPI 3.0.1 specification at /v3/api-docs")
    void testOpenApiDocsEndpoint() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new OpenApiController()).build();

        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").value("3.0.1"))
                .andExpect(jsonPath("$.info.title").value("AppService Enterprise Administration REST API"))
                .andExpect(jsonPath("$.paths['/api/admin/users/search']").exists())
                .andExpect(jsonPath("$.paths['/api/admin/roles/search']").exists())
                .andExpect(jsonPath("$.paths['/api/admin/tenants/search']").exists())
                .andExpect(jsonPath("$.paths['/api/admin/audit-events/search']").exists());
    }
}
