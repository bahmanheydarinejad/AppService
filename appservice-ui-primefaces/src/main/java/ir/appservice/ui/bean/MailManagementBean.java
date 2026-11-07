package ir.appservice.ui.bean;

import ir.appservice.infrastructure.platform.mail.MailTemplateEngine;
import ir.appservice.infrastructure.platform.mail.PlatformMailEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.Map;

/**
 * Backing bean for Panel 3: Mail Management Panel & Live Template Studio.
 */
@Component("mailManagementBean")
@SessionScope
public class MailManagementBean implements Serializable {

    private final PlatformMailEngine mailEngine;
    private final MailTemplateEngine templateEngine;

    private String templateSource = "Hello {{name}},\nWelcome to {{service}}!\nStatus: {{status}}";
    private String previewHtml = "";
    private String testRecipient = "admin@appservice.internal";
    private String testSubject = "AppService Verification Test";
    private String statusMessage;

    @Autowired
    public MailManagementBean(PlatformMailEngine mailEngine, MailTemplateEngine templateEngine) {
        this.mailEngine = mailEngine;
        this.templateEngine = templateEngine;
        renderPreview();
    }

    public void renderPreview() {
        if (templateEngine != null && templateSource != null) {
            Map<String, Object> sampleModel = Map.of(
                    "name", "System Administrator",
                    "service", "AppService Core",
                    "status", "ACTIVE"
            );
            this.previewHtml = templateEngine.render(templateSource, sampleModel);
        }
    }

    public void sendTestEmail() {
        if (mailEngine != null && testRecipient != null) {
            try {
                renderPreview();
                mailEngine.sendHtmlEmail(testRecipient, testSubject, previewHtml);
                this.statusMessage = "Test email dispatched to: " + testRecipient;
            } catch (Exception e) {
                this.statusMessage = "Email dispatch failed: " + e.getMessage();
            }
        }
    }

    // Getters and Setters
    public String getTemplateSource() { return templateSource; }
    public void setTemplateSource(String templateSource) { this.templateSource = templateSource; }

    public String getPreviewHtml() { return previewHtml; }
    public void setPreviewHtml(String previewHtml) { this.previewHtml = previewHtml; }

    public String getTestRecipient() { return testRecipient; }
    public void setTestRecipient(String testRecipient) { this.testRecipient = testRecipient; }

    public String getTestSubject() { return testSubject; }
    public void setTestSubject(String testSubject) { this.testSubject = testSubject; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
}
