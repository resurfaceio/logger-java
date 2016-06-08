// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Iterator;

import static io.resurface.JsonMessage.*;

/**
 * Logger for HTTP usage.
 */
public class HttpLogger extends BaseLogger<HttpLogger> {

    /**
     * Agent string identifying this logger.
     */
    public static final String AGENT = "HttpLogger.java";

    /**
     * Initialize enabled logger using default url.
     */
    public HttpLogger() {
        super();
    }

    /**
     * Initialize enabled logger using custom url.
     */
    public HttpLogger(String url) {
        super(url);
    }

    /**
     * Initialize enabled or disabled logger using custom url.
     */
    public HttpLogger(String url, boolean enabled) {
        super(url, enabled);
    }

    /**
     * Returns agent string identifying this logger.
     */
    @Override
    public String agent() {
        return AGENT;
    }

    /**
     * Formats JSON message for simple echo.
     */
    public StringBuilder formatEcho(StringBuilder json, long now) {
        start(json, "echo", agent(), version(), now);
        return stop(json);
    }

    /**
     * Formats JSON message for HTTP request.
     */
    public StringBuilder formatRequest(StringBuilder json, long now, HttpServletRequest request, String body) {
        start(json, "http_request", agent(), version(), now).append(',');
        append(json, "method", request.getMethod()).append(',');
        appendRequestURL(json, request);
        appendRequestHeaders(json, request);
        appendBody(json, body);
        return stop(json);
    }

    /**
     * Formats JSON message for HTTP response.
     */
    public StringBuilder formatResponse(StringBuilder json, long now, HttpServletResponse response, String body) {
        start(json, "http_response", agent(), version(), now).append(',');
        append(json, "code", response.getStatus()).append(',');
        appendResponseHeaders(json, response);
        appendBody(json, body);
        return stop(json);
    }

    /**
     * Logs echo (in JSON format) to remote url.
     */
    public boolean logEcho() {
        if (enabled || tracing) {
            StringBuilder json = new StringBuilder(64);
            formatEcho(json, System.currentTimeMillis());
            return post(json.toString());
        } else {
            return true;
        }
    }

    /**
     * Logs HTTP request (in JSON format) to remote url.
     */
    public boolean logRequest(HttpServletRequest request) {
        return logRequest(request, null);
    }

    /**
     * Logs HTTP request with body (in JSON format) to remote url.
     */
    public boolean logRequest(HttpServletRequest request, String body) {
        if (enabled || tracing) {
            StringBuilder json = new StringBuilder(1024);
            formatRequest(json, System.currentTimeMillis(), request, body);
            return post(json.toString());
        } else {
            return true;
        }
    }

    /**
     * Logs HTTP response (in JSON format) to remote url.
     */
    public boolean logResponse(HttpServletResponse response) {
        return logResponse(response, null);
    }

    /**
     * Logs HTTP response with body (in JSON format) to remote url.
     */
    public boolean logResponse(HttpServletResponse response, String body) {
        if (enabled || tracing) {
            StringBuilder json = new StringBuilder(1024);
            formatResponse(json, System.currentTimeMillis(), response, body);
            return post(json.toString());
        } else {
            return true;
        }
    }

    /**
     * Adds body to message.
     */
    protected StringBuilder appendBody(StringBuilder json, String body) {
        if (body != null) {
            json.append(',');
            append(json, "body", body);
        }
        return json;
    }

    /**
     * Adds request headers to message.
     */
    protected StringBuilder appendRequestHeaders(StringBuilder json, HttpServletRequest request) {
        append(json, "headers").append(":[");
        Enumeration<String> header_names = request.getHeaderNames();
        for (int headers = 0; header_names.hasMoreElements(); ) {
            String name = header_names.nextElement();
            Enumeration<String> e = request.getHeaders(name);
            name = name.toLowerCase();
            while (e.hasMoreElements()) append(json.append(headers++ == 0 ? '{' : ",{"), name, e.nextElement()).append('}');
        }
        return json.append("]");
    }

    /**
     * Adds request URL to message.
     */
    protected StringBuilder appendRequestURL(StringBuilder json, HttpServletRequest request) {
        String queryString = request.getQueryString();
        StringBuffer url = request.getRequestURL();
        if (queryString != null) url.append('?').append(queryString);
        return append(json, "url", url.toString()).append(',');
    }

    /**
     * Adds response headers to message.
     */
    protected StringBuilder appendResponseHeaders(StringBuilder json, HttpServletResponse response) {
        append(json, "headers").append(":[");
        Iterator<String> header_names = response.getHeaderNames().iterator();
        for (int headers = 0; header_names.hasNext(); ) {
            String name = header_names.next();
            Iterator<String> i = response.getHeaders(name).iterator();
            name = name.toLowerCase();
            while (i.hasNext()) append(json.append(headers++ == 0 ? '{' : ",{"), name, i.next()).append('}');
        }
        return json.append("]");
    }

}
