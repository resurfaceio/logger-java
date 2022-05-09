// © 2016-2022 Resurface Labs Inc.

package io.resurface;

import java.io.*;

/**
 * Servlet input stream allowing data to be read more than once.
 */
public class LoggedInputStream extends javax.servlet.ServletInputStream {

    /**
     * Constructor taking original input stream to wrap.
     */
    public LoggedInputStream(InputStream input) throws IOException {
        this(input, 1024 * 1024);
    }

    /**
     * Constructor taking original input stream and limit in bytes.
     */
    public LoggedInputStream(InputStream input, int limit) throws IOException {
        if (input == null) throw new IllegalArgumentException("Null input");

        int logged_bytes = 0;
        boolean overflowed = false;

        // consume entire input stream, but enforce limit on copying
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = input.read(buf)) > 0) {
            logged_bytes += len;
            if (logged_bytes > limit) {
                overflowed = true;
            } else {
                os.write(buf, 0, len);
            }
        }

        if (overflowed) {
            os = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(os);
            pw.print("{ \"overflowed\": ");
            pw.print(logged_bytes);
            pw.print(" }");
            pw.flush();
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
