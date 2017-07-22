// © 2016-2017 Resurface Labs LLC

package io.resurface.tests;

import io.resurface.HttpServletRequestImpl;
import io.resurface.LoggedInputStream;
import io.resurface.LoggedRequestWrapper;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static org.junit.Assert.assertArrayEquals;

/**
 * Tests against servlet request wrapper for HTTP usage logging.
 */
public class LoggedRequestWrapperTest {

    @Test
    public void formParseFormCheckboxTest() throws IOException {
        LoggedRequestWrapper w = new LoggedRequestWrapper(Helper.mockRequestWithFormCheckbox());
        expect(w.getParameterMap().size()).toEqual(1);
        expect(w.getParameter("anykey")).toBeNull();
        expect(w.getParameter("a")).toEqual("A1");
        expect(w.getParameterValues("a").length).toEqual(3);
    }

    @Test
    public void formParseFormRegisterTest() throws IOException {
        LoggedRequestWrapper w = new LoggedRequestWrapper(Helper.mockRequestWithFormRegister());
        expect(w.getParameterMap().size()).toEqual(3);
        expect(w.getParameter("anykey")).toBeNull();
        expect(w.getParameter("firstname")).toEqual("wreck it");
        expect(w.getParameter("lastname")).toEqual("ralph");
        expect(w.getParameter("middle")).toEqual("");
    }

    @Test
    public void formParseNothingTest() throws IOException {
        LoggedRequestWrapper w = new LoggedRequestWrapper(Helper.mockRequest());
        expect(w.getParameterMap().size()).toEqual(0);
        expect(w.getParameter("anykey")).toBeNull();
        expect(w.getParameter("firstname")).toBeNull();
        expect(w.getParameter("lastname")).toBeNull();
    }

    @Test
    public void inputStreamClassTest() throws IOException {
        LoggedRequestWrapper w = new LoggedRequestWrapper(Helper.mockRequest());
        expect(w.getInputStream()).toBeNotNull();
        expect(w.getInputStream().getClass()).toEqual(LoggedInputStream.class);
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
        expect(w.getReader()).toBeNotNull();
        expect(w.getReader().getClass()).toEqual(BufferedReader.class);
    }

    @Test
    public void readerInputTest() throws IOException {
        byte[] test_bytes = {'W', 'T', 'F', '?'};
        LoggedRequestWrapper w = new LoggedRequestWrapper(new HttpServletRequestImpl(test_bytes));
        for (byte test_byte : test_bytes) expect(w.getReader().read()).toEqual(test_byte);
    }

}
