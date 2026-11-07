package ir.appservice.infrastructure.platform.mcp;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of MCP tools discoverable by AI agents.
 */
@Component
public class McpToolRegistry {

    private final Map<String, McpTool> tools = new ConcurrentHashMap<>();

    public void registerTool(McpTool tool) {
        if (tool != null && tool.name() != null) {
            tools.put(tool.name(), tool);
        }
    }

    public Optional<McpTool> findTool(String toolName) {
        return Optional.ofNullable(tools.get(toolName));
    }

    public Collection<McpTool> getAllTools() {
        return Collections.unmodifiableCollection(tools.values());
    }

    public boolean hasTool(String toolName) {
        return tools.containsKey(toolName);
    }
}
