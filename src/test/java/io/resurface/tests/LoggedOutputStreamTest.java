// © 2016-2018 Resurface Labs LLC

package io.resurface.tests;

import io.resurface.LoggedOutputStream;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

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
            expect(iae.getMessage()).toContain("Null output");
        }
    }

    @Test
    public void emptyOutputTest() {
        LoggedOutputStream los = new LoggedOutputStream(new ByteArrayOutputStream());
        expect(los.logged().length).toEqual(0);
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
