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
 * Tests against logger for HTTP usage.
 */
public class HttpLoggerTest {

    private HttpServletRequest buildRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://something.com/index.html"));
        return request;
    }

    private HttpServletResponse buildResponse() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        Mockito.when(response.getStatus()).thenReturn(201);
        return response;
    }

    @Test
    public void formatEchoTest() {
        String message = new HttpLogger().formatEcho(new StringBuilder(), 12345).toString();
        assertTrue("has category", message.contains("{\"category\":\"echo\","));
        assertTrue("has source", message.contains("\"source\":\"" + HttpLogger.SOURCE + "\","));
        assertTrue("has version", message.contains("\"version\":\"" + HttpLogger.version_lookup() + "\","));
        assertTrue("has now", message.contains("\"now\":12345}"));
    }

    @Test
    public void formatRequestTest() {
        String message = new HttpLogger().formatRequest(new StringBuilder(), 1455908640173L, buildRequest()).toString();
        assertTrue("has category", message.contains("{\"category\":\"http_request\","));
        assertTrue("has source", message.contains("\"source\":\"" + HttpLogger.SOURCE + "\","));
        assertTrue("has version", message.contains("\"version\":\"" + HttpLogger.version_lookup() + "\","));
        assertTrue("has now", message.contains("\"now\":1455908640173,"));
        assertTrue("has url", message.contains("\"url\":\"http://something.com/index.html\"}"));
    }

    @Test
    public void formatResponseTest() {
        String message = new HttpLogger().formatResponse(new StringBuilder(), 1455908665227L, buildResponse(), null).toString();
        assertTrue("has category", message.contains("{\"category\":\"http_response\","));
        assertTrue("has source", message.contains("\"source\":\"" + HttpLogger.SOURCE + "\","));
        assertTrue("has version", message.contains("\"version\":\"" + HttpLogger.version_lookup() + "\","));
        assertTrue("has now", message.contains("\"now\":1455908665227,"));
        assertTrue("has code", message.contains("\"code\":201}"));
        assertTrue("omits body", !message.contains("\"body\""));
    }

    @Test
    public void formatResponseWithBodyTest() {
        String body = "<html><h1>We want the funk</h1><p>Gotta have that funk</p></html>";
        String message = new HttpLogger().formatResponse(new StringBuilder(), 1455908665227L, buildResponse(), body).toString();
        assertTrue("has category", message.contains("{\"category\":\"http_response\","));
        assertTrue("has source", message.contains("\"source\":\"" + HttpLogger.SOURCE + "\","));
        assertTrue("has version", message.contains("\"version\":\"" + HttpLogger.version_lookup() + "\","));
        assertTrue("has now", message.contains("\"now\":1455908665227,"));
        assertTrue("has code", message.contains("\"code\":201,"));
        assertTrue("has body", message.contains("\"body\":\"" + body + "\"}"));
    }

    @Test
    public void logEchoToDefaultUrlTest() {
        HttpLogger logger = new HttpLogger();
        assertTrue("log echo succeeds", logger.logEcho());
        assertTrue("tracing history empty", logger.tracingHistory().size() == 0);
    }

    @Test
    public void logEchoToInvalidUrlTest() {
        HttpLogger logger = new HttpLogger(HttpLogger.URL + "/noway3is5this1valid2");
        assertTrue("log echo fails", !logger.logEcho());
        assertTrue("tracing history empty", logger.tracingHistory().size() == 0);

        logger = new HttpLogger("'https://www.noway3is5this1valid2.com/'");
        assertTrue("log echo fails", !logger.logEcho());
        assertTrue("tracing history empty", logger.tracingHistory().size() == 0);

        logger = new HttpLogger("'http://www.noway3is5this1valid2.com/'");
        assertTrue("log echo fails", !logger.logEcho());
        assertTrue("tracing history empty", logger.tracingHistory().size() == 0);
    }

    @Test
    public void skipsLoggingAndTracingWhenDisabledTest() {
        HttpLogger logger = new HttpLogger(HttpLogger.URL + "/noway3is5this1valid2", false);
        assertTrue("log echo succeeds", logger.logEcho());
        assertTrue("tracing history empty", logger.tracingHistory().size() == 0);

        logger = new HttpLogger("'https://www.noway3is5this1valid2.com/'", false);
        assertTrue("log echo succeeds", logger.logEcho());
        assertTrue("tracing history empty", logger.tracingHistory().size() == 0);

        logger = new HttpLogger("'http://www.noway3is5this1valid2.com/'", false);
        assertTrue("log echo succeeds", logger.logEcho());
        assertTrue("tracing history empty", logger.tracingHistory().size() == 0);
    }

    @Test
    public void sourceTest() {
        String source = HttpLogger.SOURCE;
        assertTrue("length check", source.length() > 0);
        assertTrue("startsWith check", source.startsWith("resurfaceio-"));
        assertTrue("backslash check", !source.contains("\\"));
        assertTrue("double quote check", !source.contains("\""));
        assertTrue("single quote check", !source.contains("'"));
    }

    @Test
    public void tracingTest() {
        HttpLogger logger = new HttpLogger().disable();
        assertTrue("logger disabled at first", !logger.isEnabled());
        assertTrue("logger tracing inactive at first", !logger.isTracing());
        assertTrue("tracing history empty", logger.tracingHistory().size() == 0);
        logger.tracingStart();
        try {
            assertTrue("logger tracing active", logger.isTracing());
            assertTrue("log echo succeeds (1)", logger.logEcho());
            assertTrue("tracing history is 1", logger.tracingHistory().size() == 1);
            assertTrue("log echo succeeds (2)", logger.logEcho());
            assertTrue("tracing history is 2", logger.tracingHistory().size() == 2);
            assertTrue("log echo succeeds (3)", logger.logEcho());
            assertTrue("tracing history is 3", logger.tracingHistory().size() == 3);
            assertTrue("log echo succeeds (4)", logger.logEcho());
            assertTrue("tracing history is 4", logger.tracingHistory().size() == 4);
        } finally {
            logger.tracingStop().enable();
            assertTrue("logger enabled at end", logger.isEnabled());
            assertTrue("logger tracing inactive at end", !logger.isTracing());
            assertTrue("tracing history empty", logger.tracingHistory().size() == 0);
        }
    }

    @Test
    public void urlTest() {
        String url = HttpLogger.URL;
        assertTrue("length check", url.length() > 0);
        assertTrue("startsWith check", url.startsWith("https://"));
        assertTrue("backslash check", !url.contains("\\"));
        assertTrue("double quote check", !url.contains("\""));
        assertTrue("single quote check", !url.contains("'"));
        assertEquals(url, new HttpLogger().url());
        assertEquals("https://foobar.com", new HttpLogger("https://foobar.com").url());
    }

    @Test
    public void versionTest() {
        String version = HttpLogger.version_lookup();
        assertTrue("null check", version != null);
        assertTrue("length check", version.length() > 0);
        assertTrue("startsWith check", version.startsWith("1.0."));
        assertTrue("backslash check", !version.contains("\\"));
        assertTrue("double quote check", !version.contains("\""));
        assertTrue("single quote check", !version.contains("'"));
        assertEquals(version, new HttpLogger().version());
    }

}
