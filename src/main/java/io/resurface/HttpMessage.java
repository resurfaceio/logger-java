// © 2016-2024 Graylog, Inc.

package io.resurface;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Message implementation for HTTP logger.
 */
public class HttpMessage {

    /**
     * Submits request and response through logger.
     */
    public static void send(HttpLogger logger, HttpServletRequest request, HttpServletResponse response) {
        send(logger, request, response, null, null, 0, 0, new HashMap<>());
    }

    /**
     * Submits request and response through logger.
     */
    public static void send(HttpLogger logger, HttpServletRequest request, HttpServletResponse response, String response_body) {
        send(logger, request, response, response_body, null, 0, 0, new HashMap<>());
    }

    /**
     * Submits request and response through logger.
     */
    public static void send(HttpLogger logger, HttpServletRequest request, HttpServletResponse response,
                            String response_body, String request_body) {
        send(logger, request, response, response_body, request_body, 0, 0, new HashMap<>());
    }

    /**
     * Submits request and response through logger.
     */
    public static void send(HttpLogger logger, HttpServletRequest request, HttpServletResponse response,
                            String response_body, String request_body, long now, double interval) {
        send(logger, request, response, response_body, request_body, now, interval, new HashMap<>());
    }

    /**
     * Submits request and response through logger.
     */
    public static void send(HttpLogger logger, HttpServletRequest request, HttpServletResponse response,
                            String response_body, String request_body, long now, double interval,
                            HashMap<String, String> customFields) {

        if (!logger.isEnabled()) return;

        // copy details from request & response
        List<String[]> message = HttpMessage.build(request, response, response_body, request_body);

        // copy data from session if configured
        if (!logger.getRules().copy_session_field.isEmpty()) {
            HttpSession ssn = request.getSession(false);
            if (ssn != null) {
                for (HttpRule r : logger.getRules().copy_session_field) {
                    Enumeration<String> names = ssn.getAttributeNames();
                    while (names.hasMoreElements()) {
                        String d = names.nextElement();
                        if (((Pattern) r.param1).matcher(d).matches()) {
                            String val = ssn.getAttribute(d).toString();
                            message.add(new String[]{"session_field:" + d.toLowerCase(), val});
                        }
                    }
                }
            }
        }

        // add timing details
        if (now == 0) now = System.currentTimeMillis();
        message.add(new String[]{"now", String.valueOf(now)});
        if (interval != 0) message.add(new String[]{"interval", String.valueOf(interval)});

        logger.submitIfPassing(message, customFields);
    }

    /**
     * Builds list of key/value pairs for HTTP request and response.
     */
    public static List<String[]> build(HttpServletRequest request, HttpServletResponse response,
                                       String response_body, String request_body) {
        List<String[]> message = new ArrayList<>();
        String method = request.getMethod();
        if (method != null) message.add(new String[]{"request_method", method});
        String formatted_url = formatURL(request);
        if (formatted_url != null) message.add(new String[]{"request_url", formatted_url});
        message.add(new String[]{"response_code", String.valueOf(response.getStatus())});
        appendRequestHeaders(message, request);
        appendRequestParams(message, request);
        appendResponseHeaders(message, response);
        if (request_body != null && !request_body.equals("")) message.add(new String[]{"request_body", request_body});
        if (response_body != null && !response_body.equals("")) message.add(new String[]{"response_body", response_body});
        return message;
    }

    /**
     * Adds request headers to message.
     */
    private static void appendRequestHeaders(List<String[]> message, HttpServletRequest request) {
        Enumeration<String> header_names = request.getHeaderNames();
        while (header_names.hasMoreElements()) {
            String name = header_names.nextElement();
            Enumeration<String> e = request.getHeaders(name);
            name = "request_header:" + name.toLowerCase();
            while (e.hasMoreElements()) message.add(new String[]{name, e.nextElement()});
        }
    }

    /**
     * Adds request params to message.
     */
    private static void appendRequestParams(List<String[]> message, HttpServletRequest request) {
        Enumeration<String> param_names = request.getParameterNames();
        while (param_names.hasMoreElements()) {
            String name = param_names.nextElement();
            String[] values = request.getParameterValues(name);
            if (values != null) {
                name = "request_param:" + name.toLowerCase();
                for (String value : values) message.add(new String[]{name, value});
            }
        }
    }

    /**
     * Adds response headers to message.
     */
    private static void appendResponseHeaders(List<String[]> message, HttpServletResponse response) {
        for (String name : response.getHeaderNames()) {
            Iterator<String> i = response.getHeaders(name).iterator();
            name = "response_header:" + name.toLowerCase();
            while (i.hasNext()) message.add(new String[]{name, i.next()});
        }
    }

    /**
     * Returns complete request URL without query string.
     */
    private static String formatURL(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        return (url == null) ? null : url.toString().split("\\?")[0];
    }

}
