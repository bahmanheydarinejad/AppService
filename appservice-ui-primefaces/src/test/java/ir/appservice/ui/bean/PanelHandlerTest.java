package ir.appservice.ui.bean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PanelHandlerTest {

    private PanelHandler panelHandler;

    @BeforeEach
    void setUp() {
        panelHandler = new PanelHandler();
    }

    @Test
    @DisplayName("STEP-8.1: PanelHandler defaults to audit log panel")
    void testDefaultPanel() {
        assertThat(panelHandler.getActivePanelName()).isEqualTo("audit");
        assertThat(panelHandler.getActivePanel()).isEqualTo("/panels/audit.xhtml");
        assertThat(panelHandler.isPanelActive("audit")).isTrue();
    }

    @Test
    @DisplayName("STEP-8.1: PanelHandler switches dynamically to all 8 specialized panels")
    void testSwitchPanel() {
        panelHandler.switchPanel("scheduler");
        assertThat(panelHandler.getActivePanelName()).isEqualTo("scheduler");
        assertThat(panelHandler.getActivePanel()).isEqualTo("/panels/scheduler.xhtml");

        panelHandler.switchPanel("mail");
        assertThat(panelHandler.getActivePanelName()).isEqualTo("mail");
        assertThat(panelHandler.getActivePanel()).isEqualTo("/panels/mail.xhtml");

        panelHandler.switchPanel("sms");
        assertThat(panelHandler.getActivePanelName()).isEqualTo("sms");
        assertThat(panelHandler.getActivePanel()).isEqualTo("/panels/sms.xhtml");

        panelHandler.switchPanel("monitoring");
        assertThat(panelHandler.getActivePanelName()).isEqualTo("monitoring");
        assertThat(panelHandler.getActivePanel()).isEqualTo("/panels/ops.xhtml");

        panelHandler.switchPanel("tenants");
        assertThat(panelHandler.getActivePanelName()).isEqualTo("tenants");
        assertThat(panelHandler.getActivePanel()).isEqualTo("/panels/tenants.xhtml");

        panelHandler.switchPanel("clients");
        assertThat(panelHandler.getActivePanelName()).isEqualTo("clients");
        assertThat(panelHandler.getActivePanel()).isEqualTo("/panels/clients.xhtml");

        panelHandler.switchPanel("ai");
        assertThat(panelHandler.getActivePanelName()).isEqualTo("ai");
        assertThat(panelHandler.getActivePanel()).isEqualTo("/panels/ai.xhtml");
    }

    @Test
    @DisplayName("STEP-8.1: PanelHandler safely falls back to default on invalid panel name")
    void testInvalidPanelFallback() {
        panelHandler.switchPanel("nonexistent_panel");
        assertThat(panelHandler.getActivePanelName()).isEqualTo("audit");
        assertThat(panelHandler.getActivePanel()).isEqualTo("/panels/audit.xhtml");

        panelHandler.switchPanel(null);
        assertThat(panelHandler.getActivePanelName()).isEqualTo("audit");
    }
}
