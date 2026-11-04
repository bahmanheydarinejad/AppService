package org.springframework.boot.web.server;

import org.springframework.http.HttpStatus;

/**
 * Compatibility bridge for Spring Boot 4.x where ErrorPage
 * was relocated to {@link org.springframework.boot.web.error.ErrorPage}.
 */
public class ErrorPage extends org.springframework.boot.web.error.ErrorPage {
    public ErrorPage(String path) {
        super(path);
    }

    public ErrorPage(HttpStatus status, String path) {
        super(status, path);
    }

    public ErrorPage(Class<? extends Throwable> exception, String path) {
        super(exception, path);
    }
}
