package ir.appservice.infrastructure.platform;

import ir.appservice.infrastructure.platform.mail.MailDeliveryLedger;
import ir.appservice.infrastructure.platform.mail.PlatformMailEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Platform Mail Engine & Template Studio Tests")
class PlatformMailEngineTest {

    private final PlatformMailEngine mailEngine = new PlatformMailEngine(null); // Simulated transport

    @Test
    @DisplayName("Template token substitution correctly replaces placeholders")
    void testTemplateInterpolation() {
        String template = "Hello {{accountName}}, please click <a href=\"{{resetLink}}\">here</a> to reset password.";
        Map<String, Object> vars = Map.of(
                "accountName", "Bahman Heydari",
                "resetLink", "https://appservice.ir/reset?token=xyz987"
        );

        String rendered = mailEngine.getTemplateEngine().render(template, vars);
        assertThat(rendered).isEqualTo("Hello Bahman Heydari, please click <a href=\"https://appservice.ir/reset?token=xyz987\">here</a> to reset password.");
    }

    @Test
    @DisplayName("Mail delivery records are appended to the ledger with SENT status")
    void testDeliveryLedgerRecording() {
        MailDeliveryLedger.DeliveryRecord record = mailEngine.sendTemplatedEmail(
                "user@appservice.ir",
                "Welcome to AppService",
                "Hello {{name}}, welcome!",
                Map.of("name", "User1")
        );

        assertThat(record.status()).isEqualTo(MailDeliveryLedger.DeliveryStatus.SENT);
        assertThat(record.recipient()).isEqualTo("user@appservice.ir");
        assertThat(mailEngine.getDeliveryLedger().countSent()).isGreaterThanOrEqualTo(1);
    }
}
