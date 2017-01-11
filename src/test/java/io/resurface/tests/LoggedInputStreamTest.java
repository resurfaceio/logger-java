// © 2016-2017 Resurface Labs LLC

package io.resurface.tests;

import io.resurface.LoggedInputStream;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Tests against servlet output stream allowing data to be read after being written/flushed.
 */
public class LoggedInputStreamTest {

    @Test
    public void badInputTest() throws IOException {
        try {
            InputStream input = null;
            //noinspection ConstantConditions
            new LoggedInputStream(input);
            fail("stream was created with null input");
        } catch (IllegalArgumentException iae) {
            assertTrue("has expected message", iae.getMessage().contains("Null input"));
        }
    }

    @Test
    public void badInputTest2() {
        try {
            byte[] input = null;
            //noinspection ConstantConditions
            new LoggedInputStream(input);
            fail("stream was created with null input");
        } catch (IllegalArgumentException iae) {
            assertTrue("has expected message", iae.getMessage().contains("Null input"));
        }
    }

    @Test
    public void emptyInputTest() throws IOException {
        byte[] test_bytes = new byte[0];
        InputStream input = new ByteArrayInputStream(test_bytes);
        LoggedInputStream lis = new LoggedInputStream(input);
        assertArrayEquals(test_bytes, lis.logged());
    }

    @Test
    public void readTest() throws IOException {
        byte[] test_bytes = "Hello World1234567890-=!@#$%^&*()_+[]{};:,.<>/?`~|".getBytes();
        InputStream input = new ByteArrayInputStream(test_bytes);
        LoggedInputStream lis = new LoggedInputStream(input);
        assertTrue("has 50 bytes available", lis.available() == 50);
        assertTrue("has expected first character (72)", lis.read() == 72);
        assertTrue("has 49 bytes available", lis.available() == 49);
        assertTrue("has expected first character (101)", lis.read() == 101);
        assertTrue("has 48 bytes available", lis.available() == 48);
        assertArrayEquals(test_bytes, lis.logged());
        assertArrayEquals(test_bytes, lis.logged());  // can read this more than once
    }

}
