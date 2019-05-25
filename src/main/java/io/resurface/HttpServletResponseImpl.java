// © 2016-2019 Resurface Labs Inc.

package io.resurface;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Mock HttpServletResponse implementation.
 */
public class HttpServletResponseImpl extends BaseServletResponseImpl {

    public HttpServletResponseImpl() {
        stream = new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                // do nothing
            }
        };
    }

    @Override
    public void addHeader(String name, String value) {
        if (headers.containsKey(name)) {
            headers.get(name).add(value);
        } else {
            setHeader(name, value);
        }
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    @Override
    public void flushBuffer() throws IOException {
        // ignore, nothing to do
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
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
    public Collection<String> getHeaders(String name) {
        return headers.getOrDefault(name, null);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return stream;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(getOutputStream());
    }

    @Override
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    @Override
    public void setContentType(String contentType) {
        setHeader("Content-Type", contentType);
    }

    @Override
    public void setHeader(String name, String value) {
        List<String> values = new ArrayList<>();
        values.add(value);
        headers.put(name, values);
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void setStatus(int status, String sm) {
        this.status = status;
    }

    private String characterEncoding;
    private final Map<String, List<String>> headers = new HashMap<>();
    private int status;
    private final ServletOutputStream stream;

}
