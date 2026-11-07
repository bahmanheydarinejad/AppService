package ir.appservice.api.rest.controller;

import ir.appservice.common.api.ApiResponse;
import ir.appservice.infrastructure.platform.mail.MailTemplateEngine;
import ir.appservice.infrastructure.platform.mail.PlatformMailEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Endpoint for Mail Management & Live Template Studio.
 */
@RestController
@RequestMapping("/api/admin/mail")
public class MailManagementController {

    private final PlatformMailEngine mailEngine;
    private final MailTemplateEngine templateEngine;

    public MailManagementController(PlatformMailEngine mailEngine, MailTemplateEngine templateEngine) {
        this.mailEngine = mailEngine;
        this.templateEngine = templateEngine;
    }

    @GetMapping("/providers")
    @PreAuthorize("hasAuthority('MAIL_READ')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getProviders() {
        List<Map<String, Object>> providers = List.of(
                Map.of("provider", "SMTP_PRIMARY", "host", "smtp.appservice.internal", "port", 587, "status", "ONLINE"),
                Map.of("provider", "AWS_SES", "region", "eu-central-1", "status", "STANDBY")
        );
        return ResponseEntity.ok(ApiResponse.success(providers, "Mail providers listed successfully"));
    }

    @PostMapping("/templates")
    @PreAuthorize("hasAuthority('MAIL_MANAGE')")
    public ResponseEntity<ApiResponse<String>> previewTemplate(@RequestBody Map<String, Object> payload) {
        String template = (String) payload.getOrDefault("template", "");
        @SuppressWarnings("unchecked")
        Map<String, Object> model = (Map<String, Object>) payload.getOrDefault("model", Map.of());
        String rendered = templateEngine.render(template, model);
        return ResponseEntity.ok(ApiResponse.success(rendered, "Template rendered successfully"));
    }

    @PostMapping("/test-send")
    @PreAuthorize("hasAuthority('MAIL_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> sendTestMail(@RequestBody Map<String, Object> payload) {
        String to = (String) payload.getOrDefault("to", "test@appservice.internal");
        String subject = (String) payload.getOrDefault("subject", "Test Email");
        String body = (String) payload.getOrDefault("body", "Test Email Body");

        mailEngine.sendHtmlEmail(to, subject, body);
        return ResponseEntity.ok(ApiResponse.success(null, "Test email dispatched successfully"));
    }
}
