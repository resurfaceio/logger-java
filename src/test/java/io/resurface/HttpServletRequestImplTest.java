// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests against HttpServletRequest implementation for custom usage logging.
 */
public class HttpServletRequestImplTest {

    @Test
    public void useRequestURL() {
        String val = "http://resurface.io/yadda";
        HttpServletRequestImpl impl = new HttpServletRequestImpl();
        assertTrue("null by default", impl.getRequestURL() == null);
        impl.setRequestURL(val);
        assertTrue("value set ok", impl.getRequestURL().toString().equals(val));
    }

}
