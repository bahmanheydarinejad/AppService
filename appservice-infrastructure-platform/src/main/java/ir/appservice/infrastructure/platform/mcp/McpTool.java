package ir.appservice.infrastructure.platform.mcp;

import java.util.Map;
import java.util.function.Function;

/**
 * Definition of an AI Agent Tool compliant with Model Context Protocol (MCP).
 */
public record McpTool(
        String name,
        String description,
        String parameterSchemaJson,
        boolean isDestructive,
        Function<Map<String, Object>, Object> handler
) {}
