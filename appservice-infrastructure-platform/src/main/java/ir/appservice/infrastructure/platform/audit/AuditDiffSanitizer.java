package ir.appservice.infrastructure.platform.audit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

/**
 * Sanitizes JSON payloads and state diffs for the tamper-resistant audit ledger.
 * Automatically masks passwords, tokens, API keys, and sensitive secrets.
 */
@Component
public class AuditDiffSanitizer {

    private static final Set<String> SENSITIVE_FIELD_NAMES = Set.of(
            "password",
            "passwd",
            "secret",
            "secretkey",
            "clientsecret",
            "token",
            "rawsecret",
            "capabilitytoken",
            "apikey",
            "authorization",
            "creditcard",
            "cvv",
            "pin"
    );

    private static final String REDACTED_MARKER = "[REDACTED]";
    private final Gson gson = new Gson();

    /**
     * Serializes any object to JSON with sensitive credentials redacted.
     */
    public String sanitizeToJson(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof String str) {
            try {
                JsonElement element = JsonParser.parseString(str);
                sanitizeJsonElement(element);
                return gson.toJson(element);
            } catch (Exception e) {
                // Raw non-JSON string
                return str;
            }
        }

        JsonElement tree = gson.toJsonTree(object);
        sanitizeJsonElement(tree);
        return gson.toJson(tree);
    }

    private void sanitizeJsonElement(JsonElement element) {
        if (element == null || element.isJsonNull() || element.isJsonPrimitive()) {
            return;
        }

        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            for (String key : obj.keySet()) {
                if (isSensitiveKey(key)) {
                    obj.addProperty(key, REDACTED_MARKER);
                } else {
                    sanitizeJsonElement(obj.get(key));
                }
            }
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement item : array) {
                sanitizeJsonElement(item);
            }
        }
    }

    public boolean isSensitiveKey(String key) {
        if (key == null) return false;
        String normalized = key.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
        return SENSITIVE_FIELD_NAMES.contains(normalized);
    }
}
