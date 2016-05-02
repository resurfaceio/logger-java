// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface.tests;

import io.resurface.HttpLogger;
import org.junit.Test;

import static io.resurface.tests.Mocks.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests against logger for HTTP usage.
 */
public class HttpLoggerTest {

    @Test
    public void agentTest() {
        String agent = HttpLogger.AGENT;
        assertTrue("length check", agent.length() > 0);
        assertTrue("endsWith check", agent.endsWith(".java"));
        assertTrue("backslash check", !agent.contains("\\"));
        assertTrue("double quote check", !agent.contains("\""));
        assertTrue("single quote check", !agent.contains("'"));
    }

    @Test
    public void formatEchoTest() {
        String s = new HttpLogger().formatEcho(new StringBuilder(), 12345).toString();
        assertTrue("has category", s.contains("{\"category\":\"echo\","));
        assertTrue("has agent", s.contains("\"agent\":\"" + HttpLogger.AGENT + "\","));
        assertTrue("has version", s.contains("\"version\":\"" + HttpLogger.version_lookup() + "\","));
        assertTrue("has now", s.contains("\"now\":12345}"));
    }

    @Test
    public void formatRequestTest() {
        String s = new HttpLogger().formatRequest(new StringBuilder(), MOCK_NOW, mockRequest(), null).toString();
        assertTrue("has category", s.contains("{\"category\":\"http_request\","));
        assertTrue("has agent", s.contains("\"agent\":\"" + HttpLogger.AGENT + "\","));
        assertTrue("has version", s.contains("\"version\":\"" + HttpLogger.version_lookup() + "\","));
        assertTrue("has now", s.contains("\"now\":" + MOCK_NOW + ","));
        assertTrue("has url", s.contains("\"url\":\"" + MOCK_URL + "\"}"));
        assertTrue("omits body", !s.contains("\"body\""));
    }

    @Test
    public void formatRequestWithBodyTest() {
        String s = new HttpLogger().formatRequest(new StringBuilder(), MOCK_NOW, mockRequest(), MOCK_JSON).toString();
        assertTrue("has category", s.contains("{\"category\":\"http_request\","));
        assertTrue("has agent", s.contains("\"agent\":\"" + HttpLogger.AGENT + "\","));
        assertTrue("has version", s.contains("\"version\":\"" + HttpLogger.version_lookup() + "\","));
        assertTrue("has now", s.contains("\"now\":" + MOCK_NOW + ","));
        assertTrue("has url", s.contains("\"url\":\"" + MOCK_URL + "\","));
        assertTrue("has body", s.contains("\"body\":\"" + MOCK_JSON_ESCAPED + "\"}"));
    }

    @Test
    public void formatResponseTest() {
        String s = new HttpLogger().formatResponse(new StringBuilder(), MOCK_NOW, mockResponse(), null).toString();
        assertTrue("has category", s.contains("{\"category\":\"http_response\","));
        assertTrue("has agent", s.contains("\"agent\":\"" + HttpLogger.AGENT + "\","));
        assertTrue("has version", s.contains("\"version\":\"" + HttpLogger.version_lookup() + "\","));
        assertTrue("has now", s.contains("\"now\":" + MOCK_NOW + ","));
        assertTrue("has code", s.contains("\"code\":200}"));
        assertTrue("omits body", !s.contains("\"body\""));
    }

    @Test
    public void formatResponseWithBodyTest() {
        String s = new HttpLogger().formatResponse(new StringBuilder(), MOCK_NOW, mockResponse(), MOCK_HTML).toString();
        assertTrue("has category", s.contains("{\"category\":\"http_response\","));
        assertTrue("has agent", s.contains("\"agent\":\"" + HttpLogger.AGENT + "\","));
        assertTrue("has version", s.contains("\"version\":\"" + HttpLogger.version_lookup() + "\","));
        assertTrue("has now", s.contains("\"now\":" + MOCK_NOW + ","));
        assertTrue("has code", s.contains("\"code\":200,"));
        assertTrue("has body", s.contains("\"body\":\"" + MOCK_HTML_ESCAPED + "\"}"));
    }

    @Test
    public void logEchoToDefaultUrlTest() {
        HttpLogger logger = new HttpLogger();
        assertTrue("log echo succeeds", logger.logEcho());
        assertTrue("tracing history empty", logger.tracingHistory().size() == 0);
    }

    @Test
    public void logEchoToInvalidUrlTest() {
        for (String url : MOCK_INVALID_URLS) {
            HttpLogger logger = new HttpLogger(url);
            assertTrue("log echo fails", !logger.logEcho());
            assertTrue("tracing history empty", logger.tracingHistory().size() == 0);
        }
    }

    @Test
    public void skipsLoggingAndTracingWhenDisabledTest() {
        for (String url : MOCK_INVALID_URLS) {
            HttpLogger logger = new HttpLogger(url, false);
            assertTrue("log echo succeeds", logger.logEcho());
            assertTrue("log request succeeds on null object", logger.logRequest(null));
            assertTrue("log request succeeds on null objects", logger.logRequest(null, null));
            assertTrue("log response succeeds on null object", logger.logResponse(null));
            assertTrue("log response succeeds on null objects", logger.logResponse(null, null));
            assertTrue("tracing history empty", logger.tracingHistory().size() == 0);
        }
    }

    @Test
    public void tracingTest() {
        HttpLogger logger = new HttpLogger().disable();
        assertTrue("logger not active at first", !logger.isActive());
        assertTrue("logger disabled at first", !logger.isEnabled());
        assertTrue("logger not tracing at first", !logger.isTracing());
        assertTrue("tracing history empty", logger.tracingHistory().size() == 0);
        logger.tracingStart();
        try {
            assertTrue("logger now active", logger.isActive());
            assertTrue("logger now tracing", logger.isTracing());
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
            assertTrue("logger active at end", logger.isActive());
            assertTrue("logger enabled at end", logger.isEnabled());
            assertTrue("logger not tracing at end", !logger.isTracing());
            assertTrue("tracing history empty", logger.tracingHistory().size() == 0);
        }
    }

    @Test
    public void urlTest() {
        String url = HttpLogger.DEFAULT_URL;
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
        assertTrue("startsWith check", version.startsWith("1.2."));
        assertTrue("backslash check", !version.contains("\\"));
        assertTrue("double quote check", !version.contains("\""));
        assertTrue("single quote check", !version.contains("'"));
        assertEquals(version, new HttpLogger().version());
    }

}
