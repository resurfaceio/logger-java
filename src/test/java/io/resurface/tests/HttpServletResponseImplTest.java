// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface.tests;

import io.resurface.HttpServletResponseImpl;
import org.junit.Test;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.Iterator;

import static org.junit.Assert.assertTrue;

/**
 * Tests against HttpServletResponse implementation for custom usage logging.
 */
public class HttpServletResponseImplTest {

    @Test
    public void useCharacterEncoding() {
        String val = "UTF-8";
        HttpServletResponseImpl impl = new HttpServletResponseImpl();
        assertTrue("null by default", impl.getCharacterEncoding() == null);
        impl.setCharacterEncoding(val);
        assertTrue("value set ok", impl.getCharacterEncoding().equals(val));
    }

    @Test
    public void useContentType() {
        HttpServletResponseImpl impl = new HttpServletResponseImpl();
        assertTrue("null by default", impl.getContentType() == null);
        assertTrue("null header by default", impl.getHeader("CONTENT-TYPE") == null);

        String val = "text/html";
        impl.setContentType(val);
        assertTrue("value set ok", impl.getContentType().equals(val));
        assertTrue("header set ok", impl.getHeader("Content-Type").equals(val));
        assertTrue("null header because of case", impl.getHeader("content-type") == null);

        impl.setContentType(null);
        assertTrue("null content type after update", impl.getContentType() == null);
        assertTrue("null header after update", impl.getHeader("content-TYPE") == null);
    }

    @Test
    public void useHeaders() {
        String key = "kenny";
        String key2 = "kyle";
        String val = "stan";
        String val2 = "cartman";

        HttpServletResponseImpl impl = new HttpServletResponseImpl();
        assertTrue("null by default", impl.getHeader(key) == null);

        impl.setHeader(key, val);
        assertTrue("key set ok", impl.getHeaderNames().iterator().next().equals(key));
        assertTrue("direct value read ok", impl.getHeader(key).equals(val));
        assertTrue("iterator value read ok", impl.getHeaders(key).iterator().next().equals(val));

        impl.setHeader(key, val2);
        assertTrue("key set ok", impl.getHeaderNames().iterator().next().equals(key));
        assertTrue("direct value2 read ok", impl.getHeader(key).equals(val2));
        assertTrue("iterator value2 read ok", impl.getHeaders(key).iterator().next().equals(val2));

        impl.addHeader(key, val);
        assertTrue("key set ok", impl.getHeaderNames().iterator().next().equals(key));
        assertTrue("direct value read ok", impl.getHeader(key).equals(val2));
        Iterator<String> i = impl.getHeaders(key).iterator();
        assertTrue("iterator value read ok", i.next().equals(val2));
        assertTrue("iterator value2 read ok", i.next().equals(val));

        impl.setHeader(key2, val2);
        i = impl.getHeaderNames().iterator();
        assertTrue("header name 2 upcased not read", impl.getHeader(key2.toUpperCase()) == null);
        assertTrue("header name 2 read ok", i.next().equals(key2));
        assertTrue("header name 1 read ok", i.next().equals(key));
    }

    @Test
    public void useOutputStream() throws IOException {
        HttpServletResponseImpl impl = new HttpServletResponseImpl();
        assertTrue("set by default", impl.getOutputStream() != null);
        assertTrue("is expected type", impl.getOutputStream() instanceof ServletOutputStream);
    }

    @Test
    public void useStatus() {
        int val = 123;
        HttpServletResponseImpl impl = new HttpServletResponseImpl();
        assertTrue("null by default", impl.getStatus() == 0);
        impl.setStatus(val);
        assertTrue("value set ok", impl.getStatus() == val);
    }

}
