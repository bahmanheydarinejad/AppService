package ir.appservice.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(0)
public class AppSecurityFilter implements Filter {


    private final Logger logger = LoggerFactory.getLogger(AppSecurityFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        logger.trace("Init SecurityFilter");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest r = (HttpServletRequest) request;

        Map<String, Object> parameters = new HashMap<>();
        Enumeration<String> params = r.getParameterNames();
        while (params.hasMoreElements()) {
            String n = params.nextElement();
            parameters.put(n, r.getParameter(n));
        }

        logger.trace(String.format("request: %s, type: %s, Param Names: %s, Query String: %s, ", r.getRequestURL(), r.getMethod(), parameters, r.getQueryString()));
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.trace("Destroy SecurityFilter");
    }
}
