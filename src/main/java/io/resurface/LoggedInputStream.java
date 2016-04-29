// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Servlet input stream allowing data to be read more than once.
 */
public class LoggedInputStream extends javax.servlet.ServletInputStream {

    /**
     * Constructor taking original raw bytes to wrap.
     */
    public LoggedInputStream(byte[] input) {
        if (input == null) throw new IllegalArgumentException("Null input");
        this.logged = input;
        this.stream = new ByteArrayInputStream(logged);
    }

    /**
     * Constructor taking original input stream to wrap.
     */
    public LoggedInputStream(InputStream input) throws IOException {
        if (input == null) throw new IllegalArgumentException("Null input");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        while ((len = input.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
        this.logged = os.toByteArray();
        this.stream = new ByteArrayInputStream(logged);
    }

    /**
     * Returns the number of bytes that can be read (or skipped over) from this input stream without blocking.
     */
    @Override
    public int available() {
        return stream.available();
    }

    /**
     * Closes this input stream and releases any system resources associated with the stream.
     */
    @Override
    public void close() throws IOException {
        logged = null;
        stream.close();
    }

    /**
     * Return raw data logged so far.
     */
    public byte[] logged() {
        return logged;
    }

    /**
     * Reads the next byte of data from the input stream.
     */
    @Override
    public int read() {
        return stream.read();
    }

    /**
     * Reads up to len bytes of data from the input stream into an array of bytes.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return stream.read(b, off, len);
    }

    private byte[] logged;
    private final ByteArrayInputStream stream;

}
