package ir.appservice.infrastructure.security.handler;

import ir.appservice.common.context.TraceContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.Instant;

/**
 * REST Access Denied Handler.
 * Emits uniform JSON 403 Forbidden response compliant with ApiResponse contract.
 */
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String traceId = TraceContextHolder.getTraceId();
        String json = String.format(
                "{\"success\":false,\"message\":\"Access Denied: %s\",\"traceId\":\"%s\",\"timestamp\":\"%s\",\"data\":null}",
                escapeJson(accessDeniedException.getMessage()),
                traceId,
                Instant.now().toString()
        );
        response.getWriter().write(json);
    }

    private String escapeJson(String input) {
        if (input == null) return "Insufficient privileges";
        return input.replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}
