// © 2016-2023 Graylog, Inc.

package io.resurface;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Servlet response wrapper for HTTP usage logging.
 */
public class LoggedResponseWrapper extends javax.servlet.http.HttpServletResponseWrapper {

    /**
     * Constructor taking original response to wrap.
     */
    public LoggedResponseWrapper(HttpServletResponse response) {
        super(response);
        this.response = response;
    }

    /**
     * Flushes any buffered output.
     */
    @Override
    public void flushBuffer() throws IOException {
        if (writer != null) writer.flush();
        super.flushBuffer();
    }

    /**
     * Returns output stream against the wrapped response.
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (stream == null) {
            stream = new LoggedOutputStream(response.getOutputStream());
        }
        return stream;
    }

    /**
     * Returns writer against the wrapped response.
     */
    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            String encoding = getCharacterEncoding();
            encoding = (encoding == null) ? "ISO-8859-1" : encoding;
            writer = new PrintWriter(new OutputStreamWriter(getOutputStream(), encoding));
        }
        return writer;
    }

    /**
     * Flushes underlying stream and returns all bytes logged so far.
     */
    public byte[] logged() {
        return stream == null ? LOGGED_NOTHING : stream.logged();
    }

    /**
     * Value returned when nothing was logged.
     */
    public static final byte[] LOGGED_NOTHING = new byte[0];

    private final HttpServletResponse response;
    private LoggedOutputStream stream;
    private PrintWriter writer;

}
