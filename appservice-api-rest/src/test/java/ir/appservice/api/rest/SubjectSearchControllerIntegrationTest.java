package ir.appservice.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.appservice.api.rest.controller.*;
import ir.appservice.api.rest.service.GenericSearchService;
import ir.appservice.common.api.PagedApiResponse;
import ir.appservice.common.search.SearchRequest;
import ir.appservice.core.domain.entity.ScheduledJob;
import ir.appservice.core.domain.entity.SmsMessage;
import ir.appservice.infrastructure.persistence.entity.AuditEventJpaEntity;
import ir.appservice.infrastructure.persistence.entity.RoleJpaEntity;
import ir.appservice.infrastructure.persistence.entity.TenantJpaEntity;
import ir.appservice.infrastructure.persistence.entity.UserJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SubjectSearchControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private GenericSearchService<UserJpaEntity, String> userService;
    private GenericSearchService<RoleJpaEntity, String> roleService;
    private GenericSearchService<TenantJpaEntity, String> tenantService;
    private GenericSearchService<AuditEventJpaEntity, String> auditService;
    private GenericSearchService<SmsMessage, String> smsService;
    private GenericSearchService<ScheduledJob, String> jobService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        userService = mock(GenericSearchService.class);
        roleService = mock(GenericSearchService.class);
        tenantService = mock(GenericSearchService.class);
        auditService = mock(GenericSearchService.class);
        smsService = mock(GenericSearchService.class);
        jobService = mock(GenericSearchService.class);
    }

    @Test
    @DisplayName("STEP-7.2: Verify POST /api/admin/users/search contract")
    void testUserSearchEndpoint() throws Exception {
        UserJpaEntity user = new UserJpaEntity();
        user.setUsername("admin");
        when(userService.search(any(SearchRequest.class)))
                .thenReturn(PagedApiResponse.of("users", List.of(user), 0, 20, 1L, 10L));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UserSearchController(userService)).build();

        mockMvc.perform(post("/api/admin/users/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":0,\"size\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.subject").value("users"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("STEP-7.2: Verify POST /api/admin/roles/search contract")
    void testRoleSearchEndpoint() throws Exception {
        RoleJpaEntity role = new RoleJpaEntity();
        role.setName("ROLE_ADMIN");
        when(roleService.search(any(SearchRequest.class)))
                .thenReturn(PagedApiResponse.of("roles", List.of(role), 0, 20, 1L, 10L));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new RoleSearchController(roleService)).build();

        mockMvc.perform(post("/api/admin/roles/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":0,\"size\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.subject").value("roles"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("STEP-7.2: Verify POST /api/admin/tenants/search contract")
    void testTenantSearchEndpoint() throws Exception {
        TenantJpaEntity tenant = new TenantJpaEntity();
        tenant.setName("Acme Corp");
        when(tenantService.search(any(SearchRequest.class)))
                .thenReturn(PagedApiResponse.of("tenants", List.of(tenant), 0, 20, 1L, 10L));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TenantSearchController(tenantService)).build();

        mockMvc.perform(post("/api/admin/tenants/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":0,\"size\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.subject").value("tenants"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("STEP-7.2: Verify POST /api/admin/audit-events/search contract")
    void testAuditEventSearchEndpoint() throws Exception {
        AuditEventJpaEntity event = new AuditEventJpaEntity();
        event.setActionVerb("USER_LOGIN");
        when(auditService.search(any(SearchRequest.class)))
                .thenReturn(PagedApiResponse.of("audit-events", List.of(event), 0, 20, 1L, 10L));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AuditEventSearchController(auditService)).build();

        mockMvc.perform(post("/api/admin/audit-events/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":0,\"size\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.subject").value("audit-events"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("STEP-7.2: Verify POST /api/admin/sms/search contract")
    void testSmsSearchEndpoint() throws Exception {
        SmsMessage sms = new SmsMessage("+989123456789", "Hello", null, "tenant-1");
        when(smsService.search(any(SearchRequest.class)))
                .thenReturn(PagedApiResponse.of("sms", List.of(sms), 0, 20, 1L, 10L));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SmsSearchController(smsService)).build();

        mockMvc.perform(post("/api/admin/sms/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":0,\"size\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.subject").value("sms"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("STEP-7.2: Verify POST /api/admin/jobs/search contract")
    void testJobSearchEndpoint() throws Exception {
        ScheduledJob job = new ScheduledJob("job-1", "nightlyClean", "MAINTENANCE", "0 0 2 * * ?", "system");
        when(jobService.search(any(SearchRequest.class)))
                .thenReturn(PagedApiResponse.of("jobs", List.of(job), 0, 20, 1L, 10L));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new JobSearchController(jobService)).build();

        mockMvc.perform(post("/api/admin/jobs/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":0,\"size\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.subject").value("jobs"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
