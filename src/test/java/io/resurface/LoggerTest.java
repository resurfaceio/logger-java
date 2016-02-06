// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests against Java library for usage logging.
 */
public class LoggerTest {
    @Test
    public void hasVersionNumber() {
        String version = Logger.getVersion();
        assertTrue("version null check", version != null);
        assertTrue("version length check", version.length() > 0);
        assertTrue("version startsWith check", version.startsWith("1.0."));
    }
}
