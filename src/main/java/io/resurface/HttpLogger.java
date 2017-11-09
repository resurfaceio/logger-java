// © 2016-2017 Resurface Labs LLC

package io.resurface;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

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
    public String format(HttpServletRequest request, HttpServletResponse response) {
        return format(request, response, null, null, System.currentTimeMillis());
    }

    /**
     * Formats HTTP request and response as JSON message.
     */
    public String format(HttpServletRequest request, HttpServletResponse response, String response_body) {
        return format(request, response, response_body, null, System.currentTimeMillis());
    }

    /**
     * Formats HTTP request and response as JSON message.
     */
    public String format(HttpServletRequest request, HttpServletResponse response, String response_body, String request_body) {
        return format(request, response, response_body, request_body, System.currentTimeMillis());
    }

    /**
     * Formats HTTP request and response as JSON message.
     */
    public String format(HttpServletRequest request, HttpServletResponse response,
                         String response_body, String request_body, long now) {
        List<String[]> message = new ArrayList<>();
        message.add(new String[]{"request_method", request.getMethod()});
        String formatted_url = formatURL(request);
        if (formatted_url != null) message.add(new String[]{"request_url", formatted_url});
        message.add(new String[]{"response_code", String.valueOf(response.getStatus())});
        appendRequestHeaders(message, request);
        appendRequestParams(message, request);
        appendResponseHeaders(message, response);
        if (request_body != null && !request_body.equals("")) message.add(new String[]{"request_body", request_body});
        if (response_body != null && !response_body.equals("")) message.add(new String[]{"response_body", response_body});
        message.add(new String[]{"agent", this.agent});
        message.add(new String[]{"version", this.version});
        message.add(new String[]{"now", String.valueOf(now)});
        return Json.stringify(message);
    }

    /**
     * Returns true if content type indicates string data.
     */
    public static boolean isStringContentType(String s) {
        return s != null && STRING_TYPES_REGEX.matcher(s).find();
    }

    /**
     * Logs HTTP request and response to intended destination.
     */
    public boolean log(HttpServletRequest request, HttpServletResponse response) {
        return !isEnabled() || submit(format(request, response, null, null));
    }

    /**
     * Logs HTTP request and response to intended destination.
     */
    public boolean log(HttpServletRequest request, HttpServletResponse response, String response_body) {
        return !isEnabled() || submit(format(request, response, response_body, null));
    }

    /**
     * Logs HTTP request and response to intended destination.
     */
    public boolean log(HttpServletRequest request, HttpServletResponse response, String response_body, String request_body) {
        return !isEnabled() || submit(format(request, response, response_body, request_body));
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
     * Adds request params to message.
     */
    protected void appendRequestParams(List<String[]> message, HttpServletRequest request) {
        Enumeration<String> param_names = request.getParameterNames();
        while (param_names.hasMoreElements()) {
            String name = param_names.nextElement();
            String[] values = request.getParameterValues(name);
            if (values != null) {
                name = "request_param." + name.toLowerCase();
                for (String value : values) message.add(new String[]{name, value});
            }
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
        StringBuffer url = request.getRequestURL();
        if (url != null) {
            String queryString = request.getQueryString();
            if (queryString != null) url.append('?').append(queryString);
        }
        return (url == null) ? null : url.toString();
    }

    private static final String STRING_TYPES = "(?i)^text/(html|plain|xml)|application/(json|soap|xml|x-www-form-urlencoded)";
    private static final Pattern STRING_TYPES_REGEX = Pattern.compile(STRING_TYPES);

}
