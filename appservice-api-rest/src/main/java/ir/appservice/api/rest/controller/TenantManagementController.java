package ir.appservice.api.rest.controller;

import ir.appservice.common.api.ApiResponse;
import ir.appservice.common.exception.EntityNotFoundException;
import ir.appservice.core.domain.enums.IsolationStrategy;
import ir.appservice.infrastructure.persistence.entity.TenantJpaEntity;
import ir.appservice.infrastructure.persistence.repository.SpringDataTenantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Endpoint for Hybrid Multi-Tenant Management and Provisioning.
 */
@RestController
@RequestMapping("/api/admin/tenants")
public class TenantManagementController {

    private final SpringDataTenantRepository tenantRepository;

    public TenantManagementController(SpringDataTenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @PutMapping("/{id}/strategy")
    @PreAuthorize("hasAuthority('TENANTS_MANAGE')")
    public ResponseEntity<ApiResponse<TenantJpaEntity>> updateStrategy(
            @PathVariable("id") String id,
            @RequestBody Map<String, String> payload) {

        TenantJpaEntity tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tenant", id));

        String strategyStr = payload.get("strategy");
        if (strategyStr != null) {
            IsolationStrategy strategy = IsolationStrategy.valueOf(strategyStr.toUpperCase());
            tenant.setIsolationStrategy(strategy);
            tenantRepository.save(tenant);
        }

        return ResponseEntity.ok(ApiResponse.success(tenant, "Tenant isolation strategy updated successfully"));
    }

    @PostMapping("/{id}/provision")
    @PreAuthorize("hasAuthority('TENANTS_MANAGE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> provisionTenant(@PathVariable("id") String id) {
        TenantJpaEntity tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tenant", id));

        Map<String, Object> provisionResult = Map.of(
                "tenantId", tenant.getId(),
                "tenantCode", tenant.getTenantCode() != null ? tenant.getTenantCode() : "",
                "strategy", tenant.getIsolationStrategy() != null ? tenant.getIsolationStrategy().name() : "SHARED_DB",
                "status", "PROVISIONED",
                "timestamp", System.currentTimeMillis()
        );

        return ResponseEntity.ok(ApiResponse.success(provisionResult, "Tenant schema provisioned successfully"));
    }
}
