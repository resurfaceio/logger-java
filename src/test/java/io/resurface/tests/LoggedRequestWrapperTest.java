// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface.tests;

import io.resurface.HttpServletRequestImpl;
import io.resurface.LoggedInputStream;
import io.resurface.LoggedRequestWrapper;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests against servlet request wrapper for HTTP usage logging.
 */
public class LoggedRequestWrapperTest {

    @Test
    public void inputStreamClassTest() throws IOException {
        LoggedRequestWrapper w = new LoggedRequestWrapper(Helper.mockRequest());
        assertTrue("input stream is present", w.getInputStream() != null);
        assertTrue("input stream is expected class", w.getInputStream().getClass().equals(LoggedInputStream.class));
    }

    @Test
    public void inputStreamInputTest() throws IOException {
        byte[] test_bytes = {1, 21, 66};
        LoggedRequestWrapper w = new LoggedRequestWrapper(new HttpServletRequestImpl(test_bytes));
        assertArrayEquals(test_bytes, w.logged());
    }

    @Test
    public void readerClassTest() throws IOException {
        LoggedRequestWrapper w = new LoggedRequestWrapper(Helper.mockRequest());
        assertTrue("reader is present", w.getReader() != null);
        assertTrue("reader is expected class", w.getReader().getClass().equals(BufferedReader.class));
    }

    @Test
    public void readerInputTest() throws IOException {
        byte[] test_bytes = {'W', 'T', 'F', '?'};
        LoggedRequestWrapper w = new LoggedRequestWrapper(new HttpServletRequestImpl(test_bytes));
        for (int i = 0; i < test_bytes.length; i++) assertTrue("read " + i, w.getReader().read() == test_bytes[i]);
    }

}
