// © 2016-2017 Resurface Labs LLC

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
        assertTrue("logger enabled at start", logger.isEnabled());
        UsageLoggers.disable();
        assertTrue("all usage loggers disabled", !UsageLoggers.isEnabled());
        assertTrue("logger disabled", !logger.isEnabled());
        UsageLoggers.enable();
        assertTrue("all usage loggers enabled", UsageLoggers.isEnabled());
        assertTrue("logger enabled", logger.isEnabled());
    }

    @Test
    public void providesDemoUrlTest() {
        String url = UsageLoggers.urlForDemo();
        assertTrue("length check", url.length() > 0);
        assertTrue("parsing check", new HttpLogger(url).isEnabled());
    }

    @Test
    public void providesEmptyDefaultUrlTest() {
        String url = UsageLoggers.urlByDefault();
        assertTrue("null check", url == null);
    }

}
