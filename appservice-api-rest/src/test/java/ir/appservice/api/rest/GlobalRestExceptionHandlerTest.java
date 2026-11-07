package ir.appservice.api.rest;

import ir.appservice.api.rest.exception.GlobalRestExceptionHandler;
import ir.appservice.common.exception.DuplicateEntityException;
import ir.appservice.common.exception.EntityNotFoundException;
import ir.appservice.common.exception.InvalidRequestException;
import ir.appservice.common.exception.TenantAccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalRestExceptionHandlerTest {

    @RestController
    @RequestMapping("/test/errors")
    static class FaultyTestController {
        @GetMapping("/not-found")
        public void throwNotFound() {
            throw new EntityNotFoundException("User", "99");
        }

        @GetMapping("/duplicate")
        public void throwDuplicate() {
            throw new DuplicateEntityException("User", "email", "admin@test.com");
        }

        @GetMapping("/tenant-denied")
        public void throwTenantDenied() {
            throw new TenantAccessDeniedException("tenant-1", "Tenant context mismatch");
        }

        @GetMapping("/access-denied")
        public void throwAccessDenied() {
            throw new AccessDeniedException("Forbidden role");
        }

        @GetMapping("/invalid-request")
        public void throwInvalidRequest() {
            throw new InvalidRequestException("Invalid search criteria");
        }
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FaultyTestController())
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("STEP-7.4: GlobalRestExceptionHandler maps EntityNotFoundException to HTTP 404")
    void testNotFoundMapped() throws Exception {
        mockMvc.perform(get("/test/errors/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Entity 'User' with identifier '99' was not found"));
    }

    @Test
    @DisplayName("STEP-7.4: GlobalRestExceptionHandler maps DuplicateEntityException to HTTP 409")
    void testDuplicateMapped() throws Exception {
        mockMvc.perform(get("/test/errors/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    @DisplayName("STEP-7.4: GlobalRestExceptionHandler maps TenantAccessDeniedException to HTTP 403")
    void testTenantDeniedMapped() throws Exception {
        mockMvc.perform(get("/test/errors/tenant-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(403));
    }

    @Test
    @DisplayName("STEP-7.4: GlobalRestExceptionHandler maps AccessDeniedException to HTTP 403")
    void testAccessDeniedMapped() throws Exception {
        mockMvc.perform(get("/test/errors/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(403));
    }

    @Test
    @DisplayName("STEP-7.4: GlobalRestExceptionHandler maps InvalidRequestException to HTTP 400")
    void testInvalidRequestMapped() throws Exception {
        mockMvc.perform(get("/test/errors/invalid-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(400));
    }
}
