// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface.tests;

import io.resurface.HttpServletRequestImpl;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import static org.junit.Assert.assertTrue;

/**
 * Tests against HttpServletRequest implementation for custom usage logging.
 */
public class HttpServletRequestImplTest {

    @Test
    public void useCharacterEncoding() throws UnsupportedEncodingException {
        String val = "UTF-8";
        HttpServletRequestImpl impl = new HttpServletRequestImpl();
        assertTrue("null by default", impl.getCharacterEncoding() == null);
        impl.setCharacterEncoding(val);
        assertTrue("value set ok", impl.getCharacterEncoding().equals(val));
    }

    @Test
    public void useContentType() {
        String val = "text/html";
        HttpServletRequestImpl impl = new HttpServletRequestImpl();
        assertTrue("null by default", impl.getContentType() == null);
        impl.setContentType(val);
        assertTrue("value set ok", impl.getContentType().equals(val));
    }

    @Test
    public void useHeaders() {
        String key = "2345";
        String key2 = "fish";
        String val = "u-turn";
        String val2 = "swell";

        HttpServletRequestImpl impl = new HttpServletRequestImpl();
        assertTrue("null by default", impl.getHeader(key) == null);

        impl.setHeader(key, val);
        assertTrue("key set ok", impl.getHeaderNames().nextElement().equals(key));
        assertTrue("direct value read ok", impl.getHeader(key).equals(val));
        assertTrue("enumeration value read ok", impl.getHeaders(key).nextElement().equals(val));

        impl.setHeader(key, val2);
        assertTrue("key set ok", impl.getHeaderNames().nextElement().equals(key));
        assertTrue("direct value2 read ok", impl.getHeader(key).equals(val2));
        assertTrue("enumeration value2 read ok", impl.getHeaders(key).nextElement().equals(val2));

        impl.addHeader(key, val);
        assertTrue("key set ok", impl.getHeaderNames().nextElement().equals(key));
        assertTrue("direct value read ok", impl.getHeader(key).equals(val2));
        Enumeration e = impl.getHeaders(key);
        assertTrue("enumeration value read ok", e.nextElement().equals(val2));
        assertTrue("enumeration value2 read ok", e.nextElement().equals(val));

        impl.setHeader(key2, val2);
        e = impl.getHeaderNames();
        assertTrue("header name 1 read ok", e.nextElement().equals(key));
        assertTrue("header name 2 read ok", e.nextElement().equals(key2));
    }

    @Test
    public void useMethod() {
        String val = "!METHOD!";
        HttpServletRequestImpl impl = new HttpServletRequestImpl();
        assertTrue("null by default", impl.getMethod() == null);
        impl.setMethod(val);
        assertTrue("value set ok", impl.getMethod().equals(val));
    }

    @Test
    public void useRequestURL() {
        String val = "http://resurface.io/yadda";
        HttpServletRequestImpl impl = new HttpServletRequestImpl();
        assertTrue("null by default", impl.getRequestURL() == null);
        impl.setRequestURL(val);
        assertTrue("value set ok", impl.getRequestURL().toString().equals(val));
    }

}
