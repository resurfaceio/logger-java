// © 2016-2017 Resurface Labs LLC

package io.resurface;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Usage logger for HTTP/HTTPS protocol.
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
     * Formats HTTP request and response as JSON message.
     */
    public String format(HttpServletRequest request, String request_body,
                         HttpServletResponse response, String response_body) {
        return format(request, request_body, response, response_body, System.currentTimeMillis());
    }

    /**
     * Formats HTTP request and response as JSON message.
     */
    public String format(HttpServletRequest request, String request_body,
                         HttpServletResponse response, String response_body, long now) {
        List<String[]> message = new ArrayList<>();
        message.add(new String[]{"request_method", request.getMethod()});
        message.add(new String[]{"request_url", formatURL(request)});
        message.add(new String[]{"response_code", String.valueOf(response.getStatus())});
        appendRequestHeaders(message, request);
        appendResponseHeaders(message, response);
        if (request_body != null) message.add(new String[]{"request_body", request_body});
        if (response_body != null) message.add(new String[]{"response_body", response_body});
        message.add(new String[]{"agent", this.agent});
        message.add(new String[]{"version", this.version});
        message.add(new String[]{"now", String.valueOf(now)});
        return Json.stringify(message);
    }

    /**
     * Logs HTTP request and response to intended destination.
     */
    public boolean log(HttpServletRequest request, String request_body,
                       HttpServletResponse response, String response_body) {
        return !isEnabled() || submit(format(request, request_body, response, response_body));
    }

    /**
     * Adds request headers to message.
     */
    protected void appendRequestHeaders(List<String[]> message, HttpServletRequest request) {
        Enumeration<String> header_names = request.getHeaderNames();
        while (header_names.hasMoreElements()) {
            String name = header_names.nextElement();
            Enumeration<String> e = request.getHeaders(name);
            name = "request_header." + name.toLowerCase();
            while (e.hasMoreElements()) message.add(new String[]{name, e.nextElement()});
        }
    }

    /**
     * Adds response headers to message.
     */
    protected void appendResponseHeaders(List<String[]> message, HttpServletResponse response) {
        for (String name : response.getHeaderNames()) {
            Iterator<String> i = response.getHeaders(name).iterator();
            name = "response_header." + name.toLowerCase();
            while (i.hasNext()) message.add(new String[]{name, i.next()});
        }
    }

    /**
     * Returns complete request URL including query string.
     */
    protected String formatURL(HttpServletRequest request) {
        String queryString = request.getQueryString();
        StringBuffer url = request.getRequestURL();
        if (queryString != null) url.append('?').append(queryString);
        return url.toString();
    }

}
