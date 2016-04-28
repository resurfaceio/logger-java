// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Tests against servlet output stream allowing data to be read after being written/flushed.
 */
public class LoggedOutputStreamTest {

    @Test
    public void badOutputTest() {
        try {
            new LoggedOutputStream(null);
            fail("stream was created with null output");
        } catch (IllegalArgumentException iae) {
            assertTrue("has expected message", iae.getMessage().contains("Null output"));
        }
    }

    @Test
    public void emptyOutputTest() {
        LoggedOutputStream los = new LoggedOutputStream(new ByteArrayOutputStream());
        assertTrue("logged is zero length", los.logged().length == 0);
    }

    @Test
    public void readWriteTest() throws IOException {
        LoggedOutputStream los = new LoggedOutputStream(new ByteArrayOutputStream());
        byte[] test_bytes = "Hello World".getBytes();
        los.write(test_bytes);
        assertArrayEquals(test_bytes, los.logged());
    }

    @Test
    public void readWriteAndFlushTest() throws IOException {
        LoggedOutputStream los = new LoggedOutputStream(new ByteArrayOutputStream());
        byte[] test_bytes = "Hello World1234567890-=!@#$%^&*()_+[]{};:,.<>/?`~|".getBytes();
        los.write(test_bytes);
        los.flush();
        assertArrayEquals(test_bytes, los.logged());
    }

}
