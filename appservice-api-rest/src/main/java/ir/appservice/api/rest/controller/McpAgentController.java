package ir.appservice.api.rest.controller;

import ir.appservice.common.api.ApiResponse;
import ir.appservice.infrastructure.platform.mcp.McpExecutionHarness;
import ir.appservice.infrastructure.platform.mcp.McpTool;
import ir.appservice.infrastructure.platform.mcp.McpToolRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * REST Endpoint for AI Agent Infrastructure Plane, MCP Discovery, and Sandboxed Tool Invocations.
 */
@RestController
@RequestMapping("/api/admin/ai")
public class McpAgentController {

    private final McpExecutionHarness executionHarness;
    private final McpToolRegistry toolRegistry;

    public McpAgentController(McpExecutionHarness executionHarness, McpToolRegistry toolRegistry) {
        this.executionHarness = executionHarness;
        this.toolRegistry = toolRegistry;
    }

    @GetMapping("/agents")
    @PreAuthorize("hasAuthority('AI_MANAGE')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listAgents() {
        List<Map<String, Object>> agents = List.of(
                Map.of("agentId", "agent-orchestrator", "role", "System Assistant", "tokenBudget", 100000, "status", "ACTIVE"),
                Map.of("agentId", "agent-reviewer", "role", "Code Reviewer", "tokenBudget", 50000, "status", "ACTIVE")
        );
        return ResponseEntity.ok(ApiResponse.success(agents, "AI agents listed successfully"));
    }

    @GetMapping("/mcp/tools")
    @PreAuthorize("hasAuthority('AI_TOOLS_READ')")
    public ResponseEntity<ApiResponse<Collection<McpTool>>> listTools() {
        Collection<McpTool> tools = toolRegistry.getAllTools();
        return ResponseEntity.ok(ApiResponse.success(tools, "MCP tools discovered successfully"));
    }

    @PostMapping("/mcp/invoke")
    @PreAuthorize("hasAuthority('AI_INVOKE')")
    public ResponseEntity<ApiResponse<McpExecutionHarness.ToolExecutionResult>> invokeTool(@RequestBody Map<String, Object> payload) {
        String agentId = (String) payload.getOrDefault("agentId", "agent-orchestrator");
        String toolName = (String) payload.get("toolName");
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) payload.getOrDefault("parameters", Map.of());
        String approvalToken = (String) payload.get("approvalToken");
        int tokens = payload.containsKey("estimatedTokens") ? ((Number) payload.get("estimatedTokens")).intValue() : 100;

        McpExecutionHarness.ToolExecutionResult result = executionHarness.executeTool(
                agentId, toolName, params, approvalToken, tokens
        );

        return ResponseEntity.ok(ApiResponse.success(result, "Tool executed successfully"));
    }
}
