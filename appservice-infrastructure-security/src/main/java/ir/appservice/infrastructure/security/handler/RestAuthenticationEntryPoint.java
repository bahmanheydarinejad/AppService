package ir.appservice.infrastructure.security.handler;

import ir.appservice.common.context.TraceContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.Instant;

/**
 * REST Authentication Entry Point.
 * Emits uniform JSON 401 Unauthorized response compliant with ApiResponse contract.
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String traceId = TraceContextHolder.getTraceId();
        String json = String.format(
                "{\"success\":false,\"message\":\"Unauthorized: %s\",\"traceId\":\"%s\",\"timestamp\":\"%s\",\"data\":null}",
                escapeJson(authException.getMessage()),
                traceId,
                Instant.now().toString()
        );
        response.getWriter().write(json);
    }

    private String escapeJson(String input) {
        if (input == null) return "Invalid or missing token";
        return input.replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}
