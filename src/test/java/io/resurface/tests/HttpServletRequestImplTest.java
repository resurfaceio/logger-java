// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface.tests;

import io.resurface.HttpServletRequestImpl;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

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
    public void useRequestURL() {
        String val = "http://resurface.io/yadda";
        HttpServletRequestImpl impl = new HttpServletRequestImpl();
        assertTrue("null by default", impl.getRequestURL() == null);
        impl.setRequestURL(val);
        assertTrue("value set ok", impl.getRequestURL().toString().equals(val));
    }

}
