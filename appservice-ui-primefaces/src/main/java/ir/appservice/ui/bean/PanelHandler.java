package ir.appservice.ui.bean;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Managed bean orchestrating single-page dynamic AJAX panel navigation.
 * Controls the `<ui:include src="#{panelHandler.activePanel}"/>` lifecycle without full page reloads.
 */
@Component("panelHandler")
@SessionScope
public class PanelHandler implements Serializable {

    private static final String PANEL_BASE_PATH = "/panels/";
    private static final String DEFAULT_PANEL = "audit";

    private static final Map<String, String> PANEL_MAP = Map.of(
            "audit", "/panels/audit.xhtml",
            "scheduler", "/panels/scheduler.xhtml",
            "mail", "/panels/mail.xhtml",
            "sms", "/panels/sms.xhtml",
            "monitoring", "/panels/ops.xhtml",
            "ops", "/panels/ops.xhtml",
            "tenants", "/panels/tenants.xhtml",
            "clients", "/panels/clients.xhtml",
            "ai", "/panels/ai.xhtml"
    );

    private String activePanelName = DEFAULT_PANEL;
    private String activePanel = PANEL_MAP.get(DEFAULT_PANEL);

    public void switchPanel(String panelName) {
        if (panelName != null) {
            String normalized = panelName.toLowerCase().trim();
            if (PANEL_MAP.containsKey(normalized)) {
                this.activePanelName = normalized;
                this.activePanel = PANEL_MAP.get(normalized);
                return;
            }
        }
        this.activePanelName = DEFAULT_PANEL;
        this.activePanel = PANEL_MAP.get(DEFAULT_PANEL);
    }

    public String getActivePanel() {
        return activePanel;
    }

    public void setActivePanel(String activePanel) {
        this.activePanel = activePanel;
    }

    public String getActivePanelName() {
        return activePanelName;
    }

    public void setActivePanelName(String activePanelName) {
        this.activePanelName = activePanelName;
    }

    public boolean isPanelActive(String panelName) {
        return activePanelName != null && activePanelName.equalsIgnoreCase(panelName);
    }

    public List<String> getAvailablePanels() {
        return List.of("audit", "scheduler", "mail", "sms", "monitoring", "tenants", "clients", "ai");
    }
}
