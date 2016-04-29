// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

    private BufferedReader reader;
    private final LoggedInputStream stream;

}
