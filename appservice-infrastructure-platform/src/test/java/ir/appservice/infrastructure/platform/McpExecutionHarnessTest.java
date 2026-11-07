package ir.appservice.infrastructure.platform;

import ir.appservice.core.domain.enums.Outcome;
import ir.appservice.infrastructure.platform.mcp.McpExecutionHarness;
import ir.appservice.infrastructure.platform.mcp.McpTool;
import ir.appservice.infrastructure.platform.mcp.McpToolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AI Agent MCP Execution Harness & Guardrails Tests")
class McpExecutionHarnessTest {

    private McpToolRegistry registry;
    private McpExecutionHarness harness;

    @BeforeEach
    void setUp() {
        registry = new McpToolRegistry();
        harness = new McpExecutionHarness(registry, null);

        // Safe tool (Read-only)
        registry.registerTool(new McpTool(
                "query_user_count",
                "Queries count of active platform users",
                "{}",
                false, // Non-destructive
                args -> 1420L
        ));

        // Destructive tool (Requires elevation)
        registry.registerTool(new McpTool(
                "purge_tenant_database",
                "Deletes all tenant tables and data permanently",
                "{\"properties\":{\"tenantId\":{\"type\":\"string\"}}}",
                true, // Destructive!
                args -> "PURGE_COMPLETE"
        ));
    }

    @Test
    @DisplayName("Non-destructive tool executes successfully without elevation token")
    void testSafeToolExecution() {
        McpExecutionHarness.ToolExecutionResult result = harness.executeTool(
                "agent_antigravity",
                "query_user_count",
                Map.of(),
                null,
                150
        );

        assertThat(result.success()).isTrue();
        assertThat(result.output()).isEqualTo(1420L);
        assertThat(result.tokenCount()).isEqualTo(150);
        assertThat(harness.getAgentTotalTokens("agent_antigravity")).isEqualTo(150L);

        assertThat(harness.getToolAuditLedger()).hasSize(1);
        assertThat(harness.getToolAuditLedger().get(0).outcome()).isEqualTo(Outcome.SUCCESS);
    }

    @Test
    @DisplayName("Destructive tool is blocked without human approval token")
    void testDestructiveToolRequiresApproval() {
        McpExecutionHarness.ToolExecutionResult blocked = harness.executeTool(
                "agent_antigravity",
                "purge_tenant_database",
                Map.of("tenantId", "tenant_test"),
                null, // No approval token!
                500
        );

        assertThat(blocked.success()).isFalse();
        assertThat(blocked.elevationRequired()).isTrue();
        assertThat(blocked.errorMessage()).contains("requires human elevation token");

        assertThat(harness.getToolAuditLedger()).hasSize(1);
        assertThat(harness.getToolAuditLedger().get(0).outcome()).isEqualTo(Outcome.BLOCKED);
    }

    @Test
    @DisplayName("Destructive tool succeeds when valid human approval token is provided")
    void testDestructiveToolWithApproval() {
        McpExecutionHarness.ToolExecutionResult success = harness.executeTool(
                "agent_antigravity",
                "purge_tenant_database",
                Map.of("tenantId", "tenant_test"),
                "human_approved_token_admin_9921",
                500
        );

        assertThat(success.success()).isTrue();
        assertThat(success.output()).isEqualTo("PURGE_COMPLETE");
        assertThat(harness.getAgentTotalTokens("agent_antigravity")).isEqualTo(500L);
    }
}
