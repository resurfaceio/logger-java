// © 2016-2023 Resurface Labs Inc.

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

    @Test
    public void writeOverflowByteArrayTest() throws IOException {
        byte[] testb = {0x00};
        LoggedOutputStream los = new LoggedOutputStream(new ByteArrayOutputStream(), 100);
        for (int i = 1; i <= 100; i++) {
            los.write(testb);
            expect(los.overflowed()).toBeFalse();
        }
        los.write(testb);
        expect(los.overflowed()).toBeTrue();
        expect(new String(los.logged())).toEqual("{ \"overflowed\": 101 }");
        los.write(testb);
        expect(los.overflowed()).toBeTrue();
        expect(new String(los.logged())).toEqual("{ \"overflowed\": 102 }");
    }

    @Test
    public void writeOverflowByteArrayWithOffsetTest() throws IOException {
        byte[] testb = {0x00, 0x01, 0x02};
        LoggedOutputStream los = new LoggedOutputStream(new ByteArrayOutputStream(), 100);
        for (int i = 1; i <= 50; i++) {
            los.write(testb, 1, 2);
            expect(los.overflowed()).toBeFalse();
        }
        los.write(testb, 1, 2);
        expect(los.overflowed()).toBeTrue();
        expect(new String(los.logged())).toEqual("{ \"overflowed\": 102 }");
        los.write(testb, 1, 2);
        expect(los.overflowed()).toBeTrue();
        expect(new String(los.logged())).toEqual("{ \"overflowed\": 104 }");
    }

    @Test
    public void writeOverflowIntTest() throws IOException {
        LoggedOutputStream los = new LoggedOutputStream(new ByteArrayOutputStream(), 100);
        for (int i = 1; i <= 25; i++) {
            los.write(0);
            expect(los.overflowed()).toBeFalse();
        }
        los.write(0);
        expect(los.overflowed()).toBeTrue();
        expect(new String(los.logged())).toEqual("{ \"overflowed\": 104 }");
        los.write(0);
        expect(los.overflowed()).toBeTrue();
        expect(new String(los.logged())).toEqual("{ \"overflowed\": 108 }");
    }
}
