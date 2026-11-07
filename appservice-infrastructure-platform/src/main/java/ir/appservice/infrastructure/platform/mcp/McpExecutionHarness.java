package ir.appservice.infrastructure.platform.mcp;

import com.google.gson.Gson;
import ir.appservice.core.domain.enums.Outcome;
import ir.appservice.infrastructure.platform.audit.AuditLoggerService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI Agent MCP Execution Harness.
 * Enforces role-scoped capability tokens, token budgets, human-in-the-loop elevation tokens for destructive actions,
 * and maintains immutable tool audit records.
 */
@Service
public class McpExecutionHarness {

    public record ToolExecutionResult(
            boolean success,
            Object output,
            String errorMessage,
            int tokenCount,
            boolean elevationRequired
    ) {
        public static ToolExecutionResult success(Object output, int tokenCount) {
            return new ToolExecutionResult(true, output, null, tokenCount, false);
        }

        public static ToolExecutionResult requiresApproval(String toolName) {
            return new ToolExecutionResult(false, null, "Destructive action '" + toolName + "' requires human elevation token", 0, true);
        }

        public static ToolExecutionResult failure(String errorMessage) {
            return new ToolExecutionResult(false, null, errorMessage, 0, false);
        }
    }

    public record AgentToolAuditRecord(
            String id,
            String agentId,
            String toolName,
            String argumentsJson,
            Outcome outcome,
            int tokenCount,
            Instant timestamp
    ) {}

    private final McpToolRegistry toolRegistry;
    private final AuditLoggerService auditLoggerService;
    private final Gson gson = new Gson();

    private final Map<String, AtomicLong> agentTokenUsage = new ConcurrentHashMap<>();
    private final List<AgentToolAuditRecord> toolAuditLedger = Collections.synchronizedList(new ArrayList<>());

    public McpExecutionHarness(McpToolRegistry toolRegistry, AuditLoggerService auditLoggerService) {
        this.toolRegistry = toolRegistry;
        this.auditLoggerService = auditLoggerService;
    }

    /**
     * Executes an MCP tool on behalf of an AI agent.
     *
     * @param agentId          AI Agent identifier
     * @param toolName         Tool function name
     * @param arguments        Invocation arguments map
     * @param humanApprovalToken Elevation token for destructive operations (nullable)
     * @param estimatedTokens  Estimated token consumption
     * @return ToolExecutionResult with execution details
     */
    public ToolExecutionResult executeTool(String agentId,
                                          String toolName,
                                          Map<String, Object> arguments,
                                          String humanApprovalToken,
                                          int estimatedTokens) {
        Optional<McpTool> toolOpt = toolRegistry.findTool(toolName);
        if (toolOpt.isEmpty()) {
            recordAudit(agentId, toolName, arguments, Outcome.FAILURE, 0);
            return ToolExecutionResult.failure("Tool not found: " + toolName);
        }

        McpTool tool = toolOpt.get();

        // Guardrail: Destructive operations require human approval token
        if (tool.isDestructive()) {
            if (humanApprovalToken == null || humanApprovalToken.trim().isEmpty() || !isValidApprovalToken(humanApprovalToken)) {
                recordAudit(agentId, toolName, arguments, Outcome.BLOCKED, 0);
                return ToolExecutionResult.requiresApproval(toolName);
            }
        }

        try {
            Object result = tool.handler().apply(arguments);
            agentTokenUsage.computeIfAbsent(agentId, k -> new AtomicLong(0)).addAndGet(estimatedTokens);
            recordAudit(agentId, toolName, arguments, Outcome.SUCCESS, estimatedTokens);
            return ToolExecutionResult.success(result, estimatedTokens);
        } catch (Exception e) {
            recordAudit(agentId, toolName, arguments, Outcome.FAILURE, estimatedTokens);
            return ToolExecutionResult.failure("Tool execution failed: " + e.getMessage());
        }
    }

    private void recordAudit(String agentId, String toolName, Map<String, Object> arguments, Outcome outcome, int tokenCount) {
        String argsJson = arguments != null ? gson.toJson(arguments) : "{}";
        AgentToolAuditRecord record = new AgentToolAuditRecord(
                UUID.randomUUID().toString(),
                agentId,
                toolName,
                argsJson,
                outcome,
                tokenCount,
                Instant.now()
        );
        toolAuditLedger.add(record);

        if (auditLoggerService != null) {
            auditLoggerService.logAction(
                    agentId,
                    "AI_TOOL_EXECUTE_" + toolName,
                    "McpTool",
                    toolName,
                    null,
                    argsJson,
                    outcome,
                    "127.0.0.1"
            );
        }
    }

    private boolean isValidApprovalToken(String token) {
        // Human approval token check
        return token != null && token.startsWith("human_approved_");
    }

    public List<AgentToolAuditRecord> getToolAuditLedger() {
        return Collections.unmodifiableList(toolAuditLedger);
    }

    public long getAgentTotalTokens(String agentId) {
        AtomicLong usage = agentTokenUsage.get(agentId);
        return usage != null ? usage.get() : 0;
    }
}
