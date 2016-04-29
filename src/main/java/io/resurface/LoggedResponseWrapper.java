// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

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
            writer = new PrintWriter(new OutputStreamWriter(getOutputStream()));
        }
        return writer;
    }

    /**
     * Return all bytes logged so far.
     */
    public byte[] logged() {
        return stream == null ? new byte[0] : stream.logged();
    }

    private final HttpServletResponse response;
    private LoggedOutputStream stream;
    private PrintWriter writer;

}
