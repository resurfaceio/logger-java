// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests against Java library for usage logging.
 */
public class LoggerTest {

    @Test
    public void formatStatus() {
        String status = new Logger().formatStatus(1234);
        assertTrue("has type", status.contains("\"type\":\"status\""));
        assertTrue("has source", status.contains("\"source\":\"resurfaceio-logger-java\""));
        assertTrue("has version", status.contains("\"version\":\"" + Logger.version_lookup() + "\""));
        assertTrue("has now", status.contains("\"now\":\"1234\""));
    }

    @Test
    public void logStatus() {
        assertEquals(true, new Logger().logStatus());
        assertEquals(false, new Logger(Logger.DEFAULT_URL + "/noway3is5this1valid2").logStatus());
        assertEquals(false, new Logger("'https://www.noway3is5this1valid2.com/'").logStatus());
        assertEquals(false, new Logger("'http://www.noway3is5this1valid2.com/'").logStatus());
    }

    @Test
    public void url() {
        String url = Logger.DEFAULT_URL;
        assertTrue("length check", url.length() > 0);
        assertTrue("startsWith check", url.startsWith("https://"));
        assertTrue("backslash check", !url.contains("\\"));
        assertTrue("double quote check", !url.contains("\""));
        assertTrue("single quote check", !url.contains("'"));
        assertEquals(url, new Logger().url());
        assertEquals("https://foobar.com", new Logger("https://foobar.com").url());
    }

    @Test
    public void version() {
        String version = Logger.version_lookup();
        assertTrue("null check", version != null);
        assertTrue("length check", version.length() > 0);
        assertTrue("startsWith check", version.startsWith("1.0."));
        assertTrue("backslash check", !version.contains("\\"));
        assertTrue("double quote check", !version.contains("\""));
        assertTrue("single quote check", !version.contains("'"));
        assertEquals(version, new Logger().version());
    }

}
