package ir.appservice.api.rest.controller;

import ir.appservice.common.api.ApiResponse;
import ir.appservice.core.domain.entity.SmsMessage;
import ir.appservice.core.domain.enums.CircuitState;
import ir.appservice.core.domain.enums.SmsProviderType;
import ir.appservice.core.domain.port.outbound.SmsGatewayPort;
import ir.appservice.infrastructure.platform.sms.SmartSmsRouter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST Endpoint for Multi-Provider SMS Management, Health Monitoring & Dynamic Dispatch.
 */
@RestController
@RequestMapping("/api/admin/sms")
public class SmsManagementController {

    private final SmartSmsRouter smsRouter;
    private volatile String routingPolicy = "COST_OPTIMIZED";

    public SmsManagementController(SmartSmsRouter smsRouter) {
        this.smsRouter = smsRouter;
    }

    @GetMapping("/providers")
    @PreAuthorize("hasAuthority('SMS_READ')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getProviders() {
        List<Map<String, Object>> providers = new ArrayList<>();
        for (SmsProviderType provider : SmsProviderType.values()) {
            CircuitState circuitState = smsRouter.getCircuitState(provider);
            providers.add(Map.of(
                    "provider", provider.name(),
                    "healthy", smsRouter.isHealthy(provider),
                    "circuitState", circuitState != null ? circuitState.name() : "CLOSED"
            ));
        }
        return ResponseEntity.ok(ApiResponse.success(providers, "SMS providers retrieved successfully"));
    }

    @GetMapping("/health")
    @PreAuthorize("hasAuthority('SMS_READ')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealth() {
        Map<String, Object> health = Map.of(
                "policy", routingPolicy,
                "primaryHealthy", smsRouter.isHealthy(SmsProviderType.KAVENEGAR),
                "secondaryHealthy", smsRouter.isHealthy(SmsProviderType.MAGFA),
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(ApiResponse.success(health, "SMS health status retrieved successfully"));
    }

    @PutMapping("/routing-policy")
    @PreAuthorize("hasAuthority('SMS_MANAGE')")
    public ResponseEntity<ApiResponse<String>> updateRoutingPolicy(@RequestBody Map<String, String> payload) {
        String policy = payload.getOrDefault("policy", "COST_OPTIMIZED");
        this.routingPolicy = policy;
        return ResponseEntity.ok(ApiResponse.success(this.routingPolicy, "SMS routing policy updated successfully"));
    }

    @PostMapping("/send")
    @PreAuthorize("hasAuthority('SMS_SEND')")
    public ResponseEntity<ApiResponse<SmsGatewayPort.SmsReceipt>> sendSms(@RequestBody Map<String, String> payload) {
        String recipient = payload.get("recipient");
        String content = payload.get("content");
        String tenantId = payload.getOrDefault("tenantId", "system");

        SmsMessage message = new SmsMessage(recipient, content, SmsProviderType.KAVENEGAR, tenantId);
        SmsGatewayPort.SmsReceipt result = smsRouter.sendSms(message);
        return ResponseEntity.ok(ApiResponse.success(result, "SMS dispatched successfully"));
    }
}
