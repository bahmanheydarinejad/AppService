package org.springframework.boot.web.embedded.tomcat;

import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;

/**
 * Compatibility bridge for Spring Boot 4.x where TomcatServletWebServerFactory
 * was relocated to {@link org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory}.
 */
public class TomcatServletWebServerFactory extends org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory
        implements ConfigurableServletWebServerFactory {
    public TomcatServletWebServerFactory() {
        super();
    }

    public TomcatServletWebServerFactory(int port) {
        super(port);
    }

    public TomcatServletWebServerFactory(String contextPath, int port) {
        super(contextPath, port);
    }
}
