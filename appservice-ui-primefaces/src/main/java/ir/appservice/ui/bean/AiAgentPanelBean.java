package ir.appservice.ui.bean;

import ir.appservice.infrastructure.platform.mcp.McpExecutionHarness;
import ir.appservice.infrastructure.platform.mcp.McpTool;
import ir.appservice.infrastructure.platform.mcp.McpToolRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Backing bean for Panel 8: AI Agent Control & MCP Tool Registry Panel.
 */
@Component("aiAgentPanelBean")
@SessionScope
public class AiAgentPanelBean implements Serializable {

    private final McpExecutionHarness executionHarness;
    private final McpToolRegistry toolRegistry;

    private List<McpTool> tools = new ArrayList<>();
    private String selectedToolName;
    private String invocationParamsJson = "{}";
    private String humanApprovalToken;
    private String lastExecutionOutput;
    private String statusMessage;

    @Autowired
    public AiAgentPanelBean(McpExecutionHarness executionHarness, McpToolRegistry toolRegistry) {
        this.executionHarness = executionHarness;
        this.toolRegistry = toolRegistry;
        loadTools();
    }

    public void loadTools() {
        if (toolRegistry != null) {
            Collection<McpTool> registered = toolRegistry.getAllTools();
            this.tools = new ArrayList<>(registered);
        }
    }

    public void invokeSelectedTool() {
        if (executionHarness != null && selectedToolName != null) {
            try {
                McpExecutionHarness.ToolExecutionResult result = executionHarness.executeTool(
                        "console-admin",
                        selectedToolName,
                        Map.of(),
                        humanApprovalToken,
                        150
                );
                this.lastExecutionOutput = result.success() ? String.valueOf(result.output()) : result.errorMessage();
                this.statusMessage = "Tool execution completed with success: " + result.success();
            } catch (Exception e) {
                this.statusMessage = "Tool invocation error: " + e.getMessage();
            }
        }
    }

    // Getters and Setters
    public List<McpTool> getTools() { return tools; }
    public String getSelectedToolName() { return selectedToolName; }
    public void setSelectedToolName(String selectedToolName) { this.selectedToolName = selectedToolName; }
    public String getInvocationParamsJson() { return invocationParamsJson; }
    public void setInvocationParamsJson(String invocationParamsJson) { this.invocationParamsJson = invocationParamsJson; }
    public String getHumanApprovalToken() { return humanApprovalToken; }
    public void setHumanApprovalToken(String humanApprovalToken) { this.humanApprovalToken = humanApprovalToken; }
    public String getLastExecutionOutput() { return lastExecutionOutput; }
    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
}
