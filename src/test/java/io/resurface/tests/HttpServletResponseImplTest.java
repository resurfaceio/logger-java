// © 2016-2021 Resurface Labs Inc.

package io.resurface.tests;

import io.resurface.HttpServletResponseImpl;
import org.junit.Test;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.Iterator;

import static com.mscharhag.oleaster.matcher.Matchers.expect;

/**
 * Tests against mock HttpServletResponse implementation.
 */
public class HttpServletResponseImplTest {

    @Test
    public void useCharacterEncodingTest() {
        String val = "UTF-8";
        HttpServletResponseImpl impl = new HttpServletResponseImpl();
        expect(impl.getCharacterEncoding()).toBeNull();
        impl.setCharacterEncoding(val);
        expect(impl.getCharacterEncoding()).toEqual(val);
    }

    @Test
    public void useContentTypeTest() {
        HttpServletResponseImpl impl = new HttpServletResponseImpl();
        expect(impl.getContentType()).toBeNull();
        expect(impl.getHeader("CONTENT-TYPE")).toBeNull();

        String val = "text/html";
        impl.setContentType(val);
        expect(impl.getContentType()).toEqual(val);
        expect(impl.getHeader("Content-Type")).toEqual(val);
        expect(impl.getHeader("content-type")).toBeNull();

        impl.setContentType(null);
        expect(impl.getContentType()).toBeNull();
        expect(impl.getHeader("content-TYPE")).toBeNull();
    }

    @Test
    public void useHeadersTest() {
        String key = "kenny";
        String key2 = "kyle";
        String val = "stan";
        String val2 = "cartman";

        HttpServletResponseImpl impl = new HttpServletResponseImpl();
        expect(impl.getHeader(key)).toBeNull();

        impl.setHeader(key, val);
        expect(impl.getHeaderNames().iterator().next()).toEqual(key);
        expect(impl.getHeader(key)).toEqual(val);
        expect(impl.getHeaders(key).iterator().next()).toEqual(val);

        impl.setHeader(key, val2);
        expect(impl.getHeaderNames().iterator().next()).toEqual(key);
        expect(impl.getHeader(key)).toEqual(val2);
        expect(impl.getHeaders(key).iterator().next()).toEqual(val2);

        impl.addHeader(key, val);
        expect(impl.getHeaderNames().iterator().next()).toEqual(key);
        expect(impl.getHeader(key)).toEqual(val2);
        Iterator<String> i = impl.getHeaders(key).iterator();
        expect(i.next()).toEqual(val2);
        expect(i.next()).toEqual(val);

        impl.setHeader(key2, val2);
        i = impl.getHeaderNames().iterator();
        expect(impl.getHeader(key2.toUpperCase())).toBeNull();
        expect(i.next()).toEqual(key2);
        expect(i.next()).toEqual(key);
    }

    @Test
    public void useOutputStreamTest() throws IOException {
        HttpServletResponseImpl impl = new HttpServletResponseImpl();
        expect(impl.getOutputStream()).toBeNotNull();
        expect(impl.getOutputStream() instanceof ServletOutputStream).toBeTrue();
    }

    @Test
    public void useStatusTest() {
        int val = 123;
        HttpServletResponseImpl impl = new HttpServletResponseImpl();
        expect(impl.getStatus()).toEqual(0);
        impl.setStatus(val);
        expect(impl.getStatus()).toEqual(val);
    }

}
