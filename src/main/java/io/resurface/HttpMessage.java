// © 2016-2019 Resurface Labs Inc.

package io.resurface;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Message implementation for HTTP logger.
 */
public class HttpMessage {

    /**
     * Builds list of key/value pairs for HTTP request and response.
     */
    public static List<String[]> build(HttpServletRequest request, HttpServletResponse response,
                                       String response_body, String request_body) {
        List<String[]> message = new ArrayList<>();
        if (request.getMethod() != null) message.add(new String[]{"request_method", request.getMethod()});
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
     * Returns complete request URL including query string.
     */
    private static String formatURL(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        if (url != null) {
            String queryString = request.getQueryString();
            if (queryString != null) url.append('?').append(queryString);
        }
        return (url == null) ? null : url.toString();
    }

}
