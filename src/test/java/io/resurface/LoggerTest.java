// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests against Java library for usage logging.
 */
public class LoggerTest {

    public HttpServletRequest buildHttpRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://something.com/index.html"));
        return request;
    }

    public HttpServletResponse buildHttpResponse() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        Mockito.when(response.getStatus()).thenReturn(201);
        return response;
    }

    @Test
    public void formatEcho() {
        String message = new Logger().formatEcho(new StringBuilder(), 1234).toString();
        assertTrue("has type", message.contains("\"type\":\"echo\""));
        assertTrue("has source", message.contains("\"source\":\"resurfaceio-logger-java\""));
        assertTrue("has version", message.contains("\"version\":\"" + Logger.version_lookup() + "\""));
        assertTrue("has now", message.contains("\"now\":1234"));
    }

    @Test
    public void formatHttpRequest() {
        String message = new Logger().formatHttpRequest(new StringBuilder(), 2345, buildHttpRequest()).toString();
        assertTrue("has type", message.contains("\"type\":\"http_request\""));
        assertTrue("has source", message.contains("\"source\":\"resurfaceio-logger-java\""));
        assertTrue("has version", message.contains("\"version\":\"" + Logger.version_lookup() + "\""));
        assertTrue("has now", message.contains("\"now\":2345"));
        assertTrue("has url", message.contains("\"url\":\"http://something.com/index.html\""));
    }

    @Test
    public void formatHttpResponse() {
        String message = new Logger().formatHttpResponse(new StringBuilder(), 3456, buildHttpResponse()).toString();
        assertTrue("has type", message.contains("\"type\":\"http_response\""));
        assertTrue("has source", message.contains("\"source\":\"resurfaceio-logger-java\""));
        assertTrue("has version", message.contains("\"version\":\"" + Logger.version_lookup() + "\""));
        assertTrue("has now", message.contains("\"now\":3456"));
        assertTrue("has url", message.contains("\"code\":201"));
    }

    @Test
    public void logEcho() {
        assertEquals(true, new Logger().logEcho());
        assertEquals(false, new Logger(Logger.DEFAULT_URL + "/noway3is5this1valid2").logEcho());
        assertEquals(false, new Logger("'https://www.noway3is5this1valid2.com/'").logEcho());
        assertEquals(false, new Logger("'http://www.noway3is5this1valid2.com/'").logEcho());
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
