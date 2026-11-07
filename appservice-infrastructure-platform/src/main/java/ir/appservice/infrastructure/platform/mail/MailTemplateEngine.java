package ir.appservice.infrastructure.platform.mail;

import java.util.Map;

/**
 * Lightweight template renderer supporting token interpolation (e.g. {{resetLink}}, {{accountName}}).
 */
public class MailTemplateEngine {

    /**
     * Replaces {{variable}} tokens in the template string with matching values from variables map.
     */
    public String render(String template, Map<String, Object> variables) {
        if (template == null || variables == null || variables.isEmpty()) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String token = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(token, value);
        }
        return result;
    }
}
