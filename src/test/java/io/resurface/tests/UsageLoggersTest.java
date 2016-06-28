// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface.tests;

import io.resurface.HttpLogger;
import io.resurface.UsageLoggers;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests against utilities for all usage loggers.
 */
public class UsageLoggersTest {

    @Test
    public void enableAndDisableAllLoggersTest() {
        HttpLogger logger = new HttpLogger(UsageLoggers.urlForDemo());
        assertTrue("logger active at start", logger.isActive());
        assertTrue("logger enabled at start", logger.isEnabled());
        UsageLoggers.disable();
        assertTrue("all usage loggers disabled", !UsageLoggers.isEnabled());
        assertTrue("logger inactive", !logger.isActive());
        assertTrue("logger enabled", logger.isEnabled());
        UsageLoggers.enable();
        assertTrue("all usage loggers enabled", UsageLoggers.isEnabled());
        assertTrue("logger active", logger.isActive());
        assertTrue("logger enabled", logger.isEnabled());
    }

    @Test
    public void urlForDemoTest() {
        String url = UsageLoggers.urlForDemo();
        assertTrue("length check", url.length() > 0);
        assertTrue("parsing check", new HttpLogger(url).isEnabled());
    }

}
