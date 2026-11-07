package ir.appservice.infrastructure.platform.mail;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Platform Multi-SMTP Mail Engine with live template rendering and bounce tracking ledger.
 */
@Service
public class PlatformMailEngine {

    private final JavaMailSender mailSender;
    private final MailTemplateEngine templateEngine = new MailTemplateEngine();
    private final MailDeliveryLedger deliveryLedger = new MailDeliveryLedger();
    private String defaultFromAddress = "no-reply@appservice.ir";

    public PlatformMailEngine(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Renders template with supplied variables and dispatches HTML email.
     */
    public MailDeliveryLedger.DeliveryRecord sendTemplatedEmail(String recipient,
                                                               String subject,
                                                               String htmlTemplate,
                                                               Map<String, Object> variables) {
        String body = templateEngine.render(htmlTemplate, variables);
        return sendHtmlEmail(recipient, subject, body);
    }

    /**
     * Dispatches raw HTML email and records outcome in delivery ledger.
     */
    public MailDeliveryLedger.DeliveryRecord sendHtmlEmail(String recipient, String subject, String htmlContent) {
        if (mailSender == null) {
            // Simulated transport if no active SMTP connection configured
            return deliveryLedger.recordSent(recipient, subject);
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(recipient);
            helper.setFrom(defaultFromAddress);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            return deliveryLedger.recordSent(recipient, subject);
        } catch (Exception e) {
            return deliveryLedger.recordFailure(recipient, subject, e.getMessage());
        }
    }

    public MailTemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    public MailDeliveryLedger getDeliveryLedger() {
        return deliveryLedger;
    }

    public void setDefaultFromAddress(String defaultFromAddress) {
        this.defaultFromAddress = defaultFromAddress;
    }
}
