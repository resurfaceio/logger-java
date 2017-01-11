// © 2016-2017 Resurface Labs LLC

package io.resurface;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * HttpServletResponse implementation for custom usage logging.
 */
public class HttpServletResponseImpl implements HttpServletResponse {

    public HttpServletResponseImpl() {
        stream = new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                // do nothing
            }
        };
    }

    @Override
    public void addCookie(Cookie cookie) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addDateHeader(String name, long date) {
        throw new UnsupportedOperationException();
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
    public void addIntHeader(String name, int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    @Override
    public String encodeRedirectUrl(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeRedirectURL(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeUrl(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeURL(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flushBuffer() throws IOException {
        // ignore, nothing to do
    }

    @Override
    public int getBufferSize() {
        throw new UnsupportedOperationException();
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
        return headers.containsKey(name) ? headers.get(name) : null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException();
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
    public boolean isCommitted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetBuffer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendError(int sc) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBufferSize(int size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    @Override
    public void setContentLength(int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContentType(String contentType) {
        setHeader("Content-Type", contentType);
    }

    @Override
    public void setDateHeader(String name, long date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeader(String name, String value) {
        List<String> values = new ArrayList<>();
        values.add(value);
        headers.put(name, values);
    }

    @Override
    public void setIntHeader(String name, int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocale(Locale loc) {
        throw new UnsupportedOperationException();
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
