// © 2016-2021 Resurface Labs Inc.

package io.resurface.tests;

import io.resurface.LoggedOutputStream;
import io.resurface.LoggedResponseWrapper;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static io.resurface.tests.Helper.mockResponse;
import static org.junit.Assert.assertArrayEquals;

/**
 * Tests against servlet response wrapper for HTTP usage logging.
 */
public class LoggedResponseWrapperTest {

    @Test
    public void outputStreamClassTest() throws IOException {
        LoggedResponseWrapper w = new LoggedResponseWrapper(mockResponse());
        expect(w.getOutputStream()).toBeNotNull();
        expect(w.getOutputStream().getClass()).toEqual(LoggedOutputStream.class);
    }

    @Test
    public void outputStreamOutputTest() throws IOException {
        byte[] test_bytes = {1, 21, 66};
        LoggedResponseWrapper w = new LoggedResponseWrapper(mockResponse());
        assertArrayEquals(LoggedResponseWrapper.LOGGED_NOTHING, w.logged());
        for (byte b : test_bytes) w.getOutputStream().write(b);
        w.flushBuffer();
        assertArrayEquals(test_bytes, w.logged());
    }

    @Test
    public void printWriterClassTest() throws IOException {
        LoggedResponseWrapper w = new LoggedResponseWrapper(mockResponse());
        expect(w.getWriter()).toBeNotNull();
        expect(w.getWriter().getClass()).toEqual(PrintWriter.class);
    }

    @Test
    public void printWriterOutputTest() throws IOException {
        String test_string = "What would Brian Boitano do?";
        LoggedResponseWrapper w = new LoggedResponseWrapper(mockResponse());
        assertArrayEquals(LoggedResponseWrapper.LOGGED_NOTHING, w.logged());
        w.getWriter().print(test_string);
        w.flushBuffer();
        assertArrayEquals(test_string.getBytes(), w.logged());
    }

    @Test
    public void printWriterWithoutFlushTest() throws IOException {
        String test_string = "I bet he'd kick an ass or two";
        LoggedResponseWrapper w = new LoggedResponseWrapper(mockResponse());
        w.getWriter().print(test_string);
        expect(w.logged().length).toEqual(0);
    }

}
