// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests against servlet output stream allowing data to be read after being written/flushed.
 */
public class LoggedOutputStreamTest {

    @Test
    public void badResponseTest() {
        try {
            new LoggedOutputStream(null);
            fail("stream was created with null response");
        } catch (IllegalArgumentException iae) {
            assertTrue("has expected message", iae.getMessage().contains("Null response"));
        }
    }

    @Test
    public void readWriteWithFlushTest() throws IOException {
        OutputStream response = new ByteArrayOutputStream();
        LoggedOutputStream los = new LoggedOutputStream(response);
        byte[] test_bytes = "Hello World1234567890-=!@#$%^&*()_+[]{};:,.<>/?`~|".getBytes();
        los.write(test_bytes);
        los.flush();
        byte[] test_read = los.read();
        assertArrayEquals(test_bytes, test_read);
    }

    @Test
    public void readWriteWithoutFlushTest() throws IOException {
        OutputStream response = new ByteArrayOutputStream();
        LoggedOutputStream los = new LoggedOutputStream(response);
        byte[] test_bytes = "Hello World".getBytes();
        los.write(test_bytes);
        byte[] test_read = los.read();
        assertArrayEquals(test_bytes, test_read);
    }

    @Test
    public void unusedReadTest() {
        OutputStream response = new ByteArrayOutputStream();
        LoggedOutputStream los = new LoggedOutputStream(response);
        byte[] logged_response = los.read();
        assertTrue("logged response is zero length", logged_response != null && logged_response.length == 0);
    }

    @Test
    public void unusedReadAndFlushTest() throws IOException {
        OutputStream response = new ByteArrayOutputStream();
        LoggedOutputStream los = new LoggedOutputStream(response);
        los.flush();
        byte[] logged_response = los.read();
        assertTrue("logged response is zero length", logged_response != null && logged_response.length == 0);
    }

}
