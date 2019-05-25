// © 2016-2019 Resurface Labs Inc.

package io.resurface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Servlet output stream allowing data to be read after being written/flushed.
 */
public class LoggedOutputStream extends javax.servlet.ServletOutputStream {

    /**
     * Constructor taking original output stream to wrap.
     */
    public LoggedOutputStream(OutputStream output) {
        if (output == null) throw new IllegalArgumentException("Null output");
        this.logged = new ByteArrayOutputStream();
        this.output = output;
    }

    /**
     * Closes this output stream and releases any system resources associated with this stream.
     */
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

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out.
     */
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

    /**
     * Return raw data logged so far.
     */
    public byte[] logged() {
        return logged.toByteArray();
    }

    /**
     * Writes the specified byte to this output stream.
     */
    @Override
    public void write(int b) throws IOException {
        try {
            output.write(b);
        } finally {
            logged.write(b);
        }
    }

    /**
     * Writes the specified byte array to this output stream.
     */
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

    /**
     * Writes len bytes from the specified byte array starting at offset off to this output stream.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            output.write(b, off, len);
        } finally {
            logged.write(b, off, len);
        }
    }

    private final ByteArrayOutputStream logged;
    private final OutputStream output;

}
