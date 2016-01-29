// Copyright (c) 2016 Resurface Labs, All Rights Reserved

package io.resurface;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests against Java library for usage logging.
 */
public class LoggerTest {
    @Test
    public void hasVersionNumber() {
        String version = new Logger().getVersion();
        assertTrue(version != null);
        assertTrue(version.length() > 0);
        assertTrue(version.startsWith("1.0."));
    }
}
