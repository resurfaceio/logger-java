// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

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
        super(AGENT);
    }

    /**
     * Initialize enabled/disabled logger using default url.
     */
    public HttpLogger(boolean enabled) {
        super(AGENT, enabled);
    }

    /**
     * Initialize enabled logger using url.
     */
    public HttpLogger(String url) {
        super(AGENT, url);
    }

    /**
     * Initialize enabled/disabled logger using url.
     */
    public HttpLogger(String url, boolean enabled) {
        super(AGENT, url, enabled);
    }

    /**
     * Initialize enabled logger using queue.
     */
    public HttpLogger(List<String> queue) {
        super(AGENT, queue);
    }

    /**
     * Initialize enabled/disabled logger using queue.
     */
    public HttpLogger(List<String> queue, boolean enabled) {
        super(AGENT, queue, enabled);
    }

    /**
     * Appends HTTP request and response at the end of the supplied JSON message.
     */
    public StringBuilder appendToBuffer(StringBuilder json, long now, HttpServletRequest request, String request_body,
                                        HttpServletResponse response, String response_body) {
        start(json, "http", getAgent(), getVersion(), now);
        append(json.append(','), "request_method", request.getMethod());
        appendRequestURL(json.append(','), request);
        appendRequestHeaders(json.append(','), request);
        if (request_body != null) append(json.append(','), "request_body", request_body);
        append(json.append(','), "response_code", response.getStatus());
        appendResponseHeaders(json.append(','), response);
        if (response_body != null) append(json.append(','), "response_body", response_body);
        return stop(json);
    }

    /**
     * Formats HTTP request and response as JSON message.
     */
    public String format(HttpServletRequest request, String request_body, HttpServletResponse response, String response_body) {
        StringBuilder sb = new StringBuilder(1024);  // todo recycle these?
        return appendToBuffer(sb, System.currentTimeMillis(), request, request_body, response, response_body).toString();
    }

    /**
     * Logs HTTP request and response to intended destination.
     */
    public boolean log(HttpServletRequest request, String request_body, HttpServletResponse response, String response_body) {
        return !isEnabled() || submit(format(request, request_body, response, response_body));
    }

    /**
     * Adds request headers to message.
     */
    protected StringBuilder appendRequestHeaders(StringBuilder json, HttpServletRequest request) {
        append(json, "request_headers").append(":[");
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
        return append(json, "request_url", url.toString());
    }

    /**
     * Adds response headers to message.
     */
    protected StringBuilder appendResponseHeaders(StringBuilder json, HttpServletResponse response) {
        append(json, "response_headers").append(":[");
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
