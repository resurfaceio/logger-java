// © 2016-2017 Resurface Labs LLC

package io.resurface;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet request wrapper for HTTP usage logging.
 */
public class LoggedRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {

    /**
     * Constructor taking original request to wrap.
     */
    public LoggedRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        stream = new LoggedInputStream(request.getInputStream());
    }

    /**
     * Returns input stream against the wrapped request.
     */
    @Override
    public ServletInputStream getInputStream() {
        return stream;
    }

    /**
     * Returns the value of a request parameter as a String, or null if the parameter does not exist.
     */
    @Override
    public String getParameter(String name) {
        if (name == null) return null;
        parseParameters();
        String[] values = parameters.get(name);
        return values == null ? null : values[0];  // todo delegate to super.getParameter(name) if result is null?
    }

    /**
     * Returns a java.util.Map of the parameters of this request.
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        parseParameters();
        return parameters;
    }

    /**
     * Returns an Enumeration of String objects containing the names of the parameters contained in this request.
     */
    @Override
    public Enumeration<String> getParameterNames() {
        parseParameters();
        return java.util.Collections.enumeration(parameters.keySet());
    }

    /**
     * Returns an array of String objects containing all of the values the given request parameter has,
     * or null if the parameter does not exist.
     */
    @Override
    public String[] getParameterValues(String name) {
        if (name == null) return null;
        parseParameters();
        return parameters.get(name);
    }

    /**
     * Returns reader against the wrapped request.
     */
    @Override
    public BufferedReader getReader() throws IOException {
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(getInputStream()));
        }
        return reader;
    }

    /**
     * Return all bytes logged so far.
     */
    public byte[] logged() {
        byte[] buffer = stream.logged();
        return buffer == null ? new byte[0] : buffer;
    }

    /**
     * Parses form parameters into internal collection.
     */
    private void parseParameters() {
        if (parameters != null) return;
        try {
            // check request before parsing
            String content_type = getRequest().getContentType();
            if (content_type == null || !content_type.toLowerCase().contains("x-www-form-urlencoded")) return;
            String encoding = getRequest().getCharacterEncoding();
            byte[] logged = stream.logged();
            if ((encoding == null) || (logged == null)) return;

            // parse form into parameters
            String body = new String(logged(), encoding);
            String[] pairs = body.split("&");
            this.parameters = new HashMap<>(pairs.length);
            for (String pair : pairs) {
                String[] fields = pair.split("=");
                String name = URLDecoder.decode(fields[0], encoding);
                String value = URLDecoder.decode(fields[1], encoding);
                String[] values = parameters.get(name);
                if (values == null) {
                    parameters.put(name, new String[]{value});
                } else {
                    int vlength = values.length;
                    String[] new_values = new String[vlength + 1];
                    System.arraycopy(values, 0, new_values, 0, vlength);
                    new_values[vlength] = value;
                    parameters.put(name, new_values);
                }
            }
        } catch (UnsupportedEncodingException uee) {
            // todo how to handle parsing exceptions?
        } finally {
            parameters = (parameters == null) ? PARAMETERS_NONE : parameters;
        }
    }

    private Map<String, String[]> parameters;
    private BufferedReader reader;
    private final LoggedInputStream stream;

    private static final Map<String, String[]> PARAMETERS_NONE = new HashMap<>(0);

}
