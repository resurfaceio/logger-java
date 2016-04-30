// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface.tests;

import io.resurface.LoggedOutputStream;
import io.resurface.LoggedResponseWrapper;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.Assert.assertTrue;

/**
 * Tests against servlet response wrapper for HTTP usage logging.
 */
public class LoggedResponseWrapperTest {

    @Test
    public void outputStreamTest() throws IOException {
        LoggedResponseWrapper w = new LoggedResponseWrapper(Mocks.mockResponse());
        assertTrue("output stream is present", w.getOutputStream() != null);
        assertTrue("output stream is proper class", w.getOutputStream().getClass().equals(LoggedOutputStream.class));
    }

    @Test
    public void printWriterTest() throws IOException {
        LoggedResponseWrapper w = new LoggedResponseWrapper(Mocks.mockResponse());
        assertTrue("print writer is present", w.getWriter() != null);
        assertTrue("print writer is proper class", w.getWriter().getClass().equals(PrintWriter.class));
    }

}
