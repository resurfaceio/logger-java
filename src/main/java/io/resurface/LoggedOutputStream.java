// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Servlet output stream allowing data to be read after being written/flushed.
 */
public class LoggedOutputStream extends javax.servlet.ServletOutputStream {

    public LoggedOutputStream(OutputStream response) {
        if (response == null) throw new IllegalArgumentException("Null response");
        this.response = response;
        this.stream = new ByteArrayOutputStream();
    }

    private final OutputStream response;
    private final ByteArrayOutputStream stream;

    @Override
    public void close() throws IOException {
        try {
            response.close();
        } finally {
            try {
                stream.close();
            } catch (IOException ioe) {
                // do nothing
            }
        }
    }

    @Override
    public void flush() throws IOException {
        try {
            response.flush();
        } finally {
            try {
                stream.flush();
            } catch (IOException ioe) {
                // do nothing
            }
        }
    }

    public byte[] read() {
        return stream.toByteArray();
    }

    @Override
    public void write(int b) throws IOException {
        try {
            response.write(b);
        } finally {
            stream.write(b);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        try {
            response.write(b);
        } finally {
            try {
                stream.write(b);
            } catch (IOException ioe) {
                // do nothing
            }
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            response.write(b, off, len);
        } finally {
            stream.write(b, off, len);
        }
    }

}
