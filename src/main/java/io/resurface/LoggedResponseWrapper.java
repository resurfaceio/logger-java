// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Servlet response wrapper for HTTP usage logging.
 * @todo missing test case
 */
public class LoggedResponseWrapper extends javax.servlet.http.HttpServletResponseWrapper {

    public LoggedResponseWrapper(HttpServletResponse response) {
        super(response);
        this.response = response;
    }

    private final HttpServletResponse response;
    private LoggedOutputStream stream;
    private PrintWriter writer;

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (stream == null) {
            stream = new LoggedOutputStream(response.getOutputStream());
        }
        return stream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(getOutputStream()));
        }
        return writer;
    }

    public byte[] read() {
        return stream == null ? "NO_RESPONSE_BODY".getBytes() : stream.read();
    }

}
