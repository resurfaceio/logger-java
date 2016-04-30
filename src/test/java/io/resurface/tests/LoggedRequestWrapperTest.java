// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface.tests;

import io.resurface.LoggedInputStream;
import io.resurface.LoggedRequestWrapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Tests against servlet request wrapper for HTTP usage logging.
 */
public class LoggedRequestWrapperTest {

    @Test
    public void inputStreamTest() throws IOException {
        LoggedRequestWrapper w = new LoggedRequestWrapper(Mocks.mockRequest());
        assertTrue("input stream is present", w.getInputStream() != null);
        assertTrue("input stream is expected class", w.getInputStream().getClass().equals(LoggedInputStream.class));
    }

}
