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
    public void getDefaultURL() {
        String url = Logger.getDefaultURL();
        assertTrue("length check", url.length() > 0);
        assertTrue("startsWith check", url.startsWith("https://"));
        assertTrue("single quote check", !url.contains("'"));
        assertTrue("double quote check", !url.contains("\""));
    }

    @Test
    public void getVersionNumber() {
        String version = Logger.getVersion();
        assertTrue("null check", version != null);
        assertTrue("length check", version.length() > 0);
        assertTrue("startsWith check", version.startsWith("1.0."));
        assertTrue("single quote check", !version.contains("'"));
        assertTrue("double quote check", !version.contains("\""));
    }

    @Test
    public void formatStatus() {
        String status = new Logger().formatStatus();
        assertTrue("has type", status.contains("\"type\":\"STATUS\""));
        assertTrue("has source", status.contains("\"source\":\"resurfaceio-logger-java\""));
        assertTrue("has version", status.contains("\"version\":\"" + Logger.getVersion() + "\""));
        assertTrue("has now", status.contains("\"now\":\""));
    }

    @Test
    public void logStatus() {
        assertEquals(true, new Logger().logStatus());
    }

    @Test
    public void logStatusToInvalidURL() {
        assertEquals(false, new Logger(Logger.getDefaultURL() + "/noway3is5this1valid2").logStatus());
    }

}
