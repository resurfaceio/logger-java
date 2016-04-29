// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface.tests;

import io.resurface.HttpServletResponseImpl;
import org.junit.Test;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

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
        String val = "text/html";
        HttpServletResponseImpl impl = new HttpServletResponseImpl();
        assertTrue("null by default", impl.getContentType() == null);
        impl.setContentType(val);
        assertTrue("value set ok", impl.getContentType().equals(val));
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
