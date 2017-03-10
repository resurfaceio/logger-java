// © 2016-2017 Resurface Labs LLC

package io.resurface.tests;

import io.resurface.UsageLoggers;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests against utilities for all usage loggers.
 */
public class UsageLoggersTest {

    @Test
    public void providesDefaultUrlTest() {
        String url = UsageLoggers.urlByDefault();
        assertTrue("null check", url == null);
    }

    @Test
    public void providesDemoUrlTest() {
        String url = UsageLoggers.urlForDemo();
        assertTrue("length check", url.length() > 0);
        assertTrue("scheme check", url.startsWith("https://"));
    }

}
