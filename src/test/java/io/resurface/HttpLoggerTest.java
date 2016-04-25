// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import org.junit.Test;

import java.io.IOException;

import static io.resurface.Mocks.MOCK_URL;
import static io.resurface.Mocks.mockRequest;
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
        String message = new HttpLogger().formatEcho(new StringBuilder(), 12345).toString();
        assertTrue("has category", message.contains("{\"category\":\"echo\","));
        assertTrue("has agent", message.contains("\"agent\":\"" + HttpLogger.AGENT + "\","));
        assertTrue("has version", message.contains("\"version\":\"" + HttpLogger.version_lookup() + "\","));
        assertTrue("has now", message.contains("\"now\":12345}"));
    }

    @Test
    public void formatRequestTest() {
        String message = new HttpLogger().formatRequest(new StringBuilder(), 1455908640173L, mockRequest()).toString();
        assertTrue("has category", message.contains("{\"category\":\"http_request\","));
        assertTrue("has agent", message.contains("\"agent\":\"" + HttpLogger.AGENT + "\","));
        assertTrue("has version", message.contains("\"version\":\"" + HttpLogger.version_lookup() + "\","));
        assertTrue("has now", message.contains("\"now\":1455908640173,"));
        assertTrue("has url", message.contains("\"url\":\"" + MOCK_URL + "\"}"));
    }

    @Test
    public void formatResponseTest() throws IOException {
        String message = new HttpLogger().formatResponse(new StringBuilder(), 1455908665227L, Mocks.mockResponse(), null).toString();
        assertTrue("has category", message.contains("{\"category\":\"http_response\","));
        assertTrue("has agent", message.contains("\"agent\":\"" + HttpLogger.AGENT + "\","));
        assertTrue("has version", message.contains("\"version\":\"" + HttpLogger.version_lookup() + "\","));
        assertTrue("has now", message.contains("\"now\":1455908665227,"));
        assertTrue("has code", message.contains("\"code\":200}"));
        assertTrue("omits body", !message.contains("\"body\""));
    }

    @Test
    public void formatResponseWithBodyTest() throws IOException {
        String body = "<html><h1>We want the funk</h1><p>Gotta have that funk</p></html>";
        String message = new HttpLogger().formatResponse(new StringBuilder(), 1455908665227L, Mocks.mockResponse(), body).toString();
        assertTrue("has category", message.contains("{\"category\":\"http_response\","));
        assertTrue("has agent", message.contains("\"agent\":\"" + HttpLogger.AGENT + "\","));
        assertTrue("has version", message.contains("\"version\":\"" + HttpLogger.version_lookup() + "\","));
        assertTrue("has now", message.contains("\"now\":1455908665227,"));
        assertTrue("has code", message.contains("\"code\":200,"));
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
        for (String url : Mocks.MOCK_INVALID_URLS) {
            HttpLogger logger = new HttpLogger(url);
            assertTrue("log echo fails", !logger.logEcho());
            assertTrue("tracing history empty", logger.tracingHistory().size() == 0);
        }
    }

    @Test
    public void skipsLoggingAndTracingWhenDisabledTest() {
        for (String url : Mocks.MOCK_INVALID_URLS) {
            HttpLogger logger = new HttpLogger(url, false);
            assertTrue("log echo succeeds", logger.logEcho());
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
