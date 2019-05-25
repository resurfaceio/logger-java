// © 2016-2019 Resurface Labs Inc.

package io.resurface;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Mock HttpServletRequest implementation.
 */
public class HttpServletRequestImpl extends BaseServletRequestImpl {

    public void addHeader(String name, String value) {
        if (headers.containsKey(name)) {
            headers.get(name).add(value);
        } else {
            setHeader(name, value);
        }
    }

    public void addParam(String name, String value) {
        if (params.containsKey(name)) {
            params.get(name).add(value);
        } else {
            setParam(name, value);
        }
    }

    @Override
    public String getContentType() {
        return getHeader("Content-Type");
    }

    @Override
    public String getHeader(String name) {
        return headers.containsKey(name) ? headers.get(name).get(0) : null;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return headers.containsKey(name) ? Collections.enumeration(headers.get(name)) : null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        return (values == null || values.length == 0) ? null : values[0];
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return java.util.Collections.enumeration(params.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        List<String> values = params.get(name);
        if (values == null) {
            return null;
        } else {
            String[] results = new String[values.size()];
            results = values.toArray(results);
            return results;
        }
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public StringBuffer getRequestURL() {
        return requestURL == null ? null : new StringBuffer(requestURL);
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (create && this.session == null) this.session = new HttpSessionImpl();
        return this.session;
    }

    public void setContentType(String contentType) {
        setHeader("Content-Type", contentType);
    }

    public void setHeader(String name, String value) {
        List<String> values = new ArrayList<>();
        values.add(value);
        headers.put(name, values);
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setParam(String name, String value) {
        List<String> values = new ArrayList<>();
        values.add(value);
        params.put(name, values);
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    private final Map<String, List<String>> headers = new HashMap<>();
    private String method;
    private final Map<String, List<String>> params = new HashMap<>();
    private String queryString;
    private String requestURL;
    private HttpSession session;

}
