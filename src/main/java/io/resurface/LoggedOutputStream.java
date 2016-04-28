// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Servlet output stream allowing data to be read after being written/flushed.
 */
public class LoggedOutputStream extends javax.servlet.ServletOutputStream {

    public LoggedOutputStream(OutputStream output) {
        if (output == null) throw new IllegalArgumentException("Null output");
        this.logged = new ByteArrayOutputStream();
        this.output = output;
    }

    private final ByteArrayOutputStream logged;
    private final OutputStream output;

    @Override
    public void close() throws IOException {
        try {
            output.close();
        } finally {
            try {
                logged.close();
            } catch (IOException ioe) {
                // do nothing
            }
        }
    }

    @Override
    public void flush() throws IOException {
        try {
            output.flush();
        } finally {
            try {
                logged.flush();
            } catch (IOException ioe) {
                // do nothing
            }
        }
    }

    public byte[] logged() {
        return logged.toByteArray();
    }

    @Override
    public void write(int b) throws IOException {
        try {
            output.write(b);
        } finally {
            logged.write(b);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        try {
            output.write(b);
        } finally {
            try {
                logged.write(b);
            } catch (IOException ioe) {
                // do nothing
            }
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            output.write(b, off, len);
        } finally {
            logged.write(b, off, len);
        }
    }

}
