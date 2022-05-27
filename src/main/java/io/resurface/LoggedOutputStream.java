// © 2016-2022 Resurface Labs Inc.

package io.resurface;

import javax.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Servlet output stream allowing data to be read after being written/flushed.
 */
public class LoggedOutputStream extends javax.servlet.ServletOutputStream {

    /**
     * Constructor taking original output stream to wrap.
     */
    public LoggedOutputStream(OutputStream output) {
        this(output, 1024 * 1024);
    }

    /**
     * Constructor taking original output stream and limit in bytes.
     */
    public LoggedOutputStream(OutputStream output, int limit) {
        if (output == null) throw new IllegalArgumentException("Null output");
        this.limit = limit;
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
        if (overflowed) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);
            pw.print("{ \"overflowed\": ");
            pw.print(logged_bytes);
            pw.print(" }");
            pw.flush();
            return baos.toByteArray();
        } else {
            return logged.toByteArray();
        }
    }

    /**
     * Returns true if data limit has been reached.
     */
    public boolean overflowed() {
        return overflowed;
    }

    /**
     * Writes the specified byte to this output stream.
     */
    @Override
    public void write(int b) throws IOException {
        try {
            output.write(b);
        } finally {
            logged_bytes += 4;
            if (logged_bytes > limit) {
                overflowed = true;
                logged = null;
            } else {
                logged.write(b);
            }
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
                logged_bytes += b.length;
                if (logged_bytes > limit) {
                    overflowed = true;
                    logged = null;
                } else {
                    logged.write(b);
                }
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
            logged_bytes += len;
            if (logged_bytes > limit) {
                overflowed = true;
                logged = null;
            } else {
                logged.write(b, off, len);
            }
        }
    }

    private final int limit;
    private ByteArrayOutputStream logged;
    private int logged_bytes = 0;
    private final OutputStream output;
    private boolean overflowed = false;

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }
}
