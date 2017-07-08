package com.rest;

import org.apache.http.*;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * https://hc.apache.org/httpcomponents-core-ga/httpcore/examples/org/apache/http/examples/HttpFileServer.java
 */
public class FileHandler implements HttpRequestHandler {

    private final String root;
    private static final Logger logger = Logger.getGlobal();

    public FileHandler(final String root) {
        this.root = root;
    }

    public void handle(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {

        String method = request.getRequestLine().getMethod();
        if (!method.equals("GET")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }
        String target = request.getRequestLine().getUri();
        logger.log(Level.INFO, "serving request for " + target);
        if (target.equals("/")) {
            target = "/index.html";
        }
        String path = root + target;
        if (getClass().getResource(path) != null) {
            logger.log(Level.INFO, "found " + path);
            response.setStatusCode(HttpStatus.SC_OK);
            response.setEntity(new InputStreamEntity(getClass().getResourceAsStream(path)));
        } else {
            logger.log(Level.INFO, "not found " + path);
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
        }
    }
}

