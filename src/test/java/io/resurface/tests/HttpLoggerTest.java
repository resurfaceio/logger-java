// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface.tests;

import io.resurface.HttpLogger;
import io.resurface.JsonMessage;
import org.junit.Test;

import static io.resurface.tests.Helper.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests against logger for HTTP usage.
 */
public class HttpLoggerTest {

    private final HttpLogger logger = new HttpLogger();

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
    public void appendRequestTest() {
        String json = logger.appendToBuffer(new StringBuilder(), MOCK_NOW, mockRequest(), null, mockResponse(), null).toString();
        assertTrue("json is valid", parseable(json));
        assertTrue("has agent", json.contains("\"agent\":\"" + HttpLogger.AGENT + "\""));
        assertTrue("has category", json.contains("\"category\":\"http\""));
        assertTrue("has now", json.contains("\"now\":\"" + MOCK_NOW + "\""));
        assertTrue("has request_body", !json.contains("\"request_body\""));
        assertTrue("has request_headers", json.contains("\"request_headers\":[]"));
        assertTrue("has request_method", json.contains("\"request_method\":\"GET\""));
        assertTrue("has request_url", json.contains("\"request_url\":\"" + MOCK_URL + "\""));
        assertTrue("has version", json.contains("\"version\":\"" + HttpLogger.version_lookup() + "\""));
    }

    @Test
    public void formatRequestWithBodyTest() {
        String json = logger.format(mockRequest(), MOCK_JSON, mockResponse(), null);
        assertTrue("json is valid", parseable(json));
        assertTrue("has agent", json.contains("\"agent\":\"" + HttpLogger.AGENT + "\""));
        assertTrue("has category", json.contains("\"category\":\"http\""));
        assertTrue("has request_body", json.contains("\"request_body\":\"" + MOCK_JSON_ESCAPED + "\""));
        assertTrue("has request_headers", json.contains("\"request_headers\":[]"));
        assertTrue("has request_method", json.contains("\"request_method\":\"GET\""));
        assertTrue("has request_url", json.contains("\"request_url\":\"" + MOCK_URL + "\""));
        assertTrue("has version", json.contains("\"version\":\"" + HttpLogger.version_lookup() + "\""));
    }

    @Test
    public void formatRequestWithEmptyBodyTest() {
        String json = logger.format(mockRequest(), "", mockResponse(), null);
        assertTrue("json is valid", parseable(json));
        assertTrue("has agent", json.contains("\"agent\":\"" + HttpLogger.AGENT + "\""));
        assertTrue("has category", json.contains("\"category\":\"http\""));
        assertTrue("has request_body", json.contains("\"request_body\":\"\""));
        assertTrue("has request_headers", json.contains("\"request_headers\":[]"));
        assertTrue("has request_method", json.contains("\"request_method\":\"GET\""));
        assertTrue("has request_url", json.contains("\"request_url\":\"" + MOCK_URL + "\""));
        assertTrue("has version", json.contains("\"version\":\"" + HttpLogger.version_lookup() + "\""));
    }

    @Test
    public void formatResponseTest() {
        String json = logger.format(mockRequest(), null, mockResponse(), null);
        assertTrue("json is valid", parseable(json));
        assertTrue("has agent", json.contains("\"agent\":\"" + HttpLogger.AGENT + "\""));
        assertTrue("has category", json.contains("\"category\":\"http\""));
        assertTrue("has response_body", !json.contains("\"response_body\""));
        assertTrue("has response_code", json.contains("\"response_code\":\"200\""));
        assertTrue("has response_headers", json.contains("\"response_headers\":[]"));
        assertTrue("has version", json.contains("\"version\":\"" + HttpLogger.version_lookup() + "\""));
    }

    @Test
    public void formatResponseWithBodyTest() {
        String json = logger.format(mockRequest(), null, mockResponse(), MOCK_HTML);
        assertTrue("json is valid", parseable(json));
        assertTrue("has agent", json.contains("\"agent\":\"" + HttpLogger.AGENT + "\""));
        assertTrue("has category", json.contains("\"category\":\"http\""));
        assertTrue("has response_body", json.contains("\"response_body\":\"" + MOCK_HTML_ESCAPED + "\""));
        assertTrue("has response_code", json.contains("\"response_code\":\"200\""));
        assertTrue("has response_headers", json.contains("\"response_headers\":[]"));
        assertTrue("has version", json.contains("\"version\":\"" + HttpLogger.version_lookup() + "\""));
    }

    @Test
    public void formatResponseWithEmptyBodyTest() {
        String json = logger.format(mockRequest(), null, mockResponse(), "");
        assertTrue("json is valid", parseable(json));
        assertTrue("has agent", json.contains("\"agent\":\"" + HttpLogger.AGENT + "\""));
        assertTrue("has category", json.contains("\"category\":\"http\""));
        assertTrue("has response_body", json.contains("\"response_body\":\"\""));
        assertTrue("has response_code", json.contains("\"response_code\":\"200\""));
        assertTrue("has response_headers", json.contains("\"response_headers\":[]"));
        assertTrue("has version", json.contains("\"version\":\"" + HttpLogger.version_lookup() + "\""));
    }

    @Test
    public void skipsLoggingWhenDisabledTest() {
        for (String url : MOCK_INVALID_URLS) {
            HttpLogger logger = new HttpLogger(url, false);
            assertTrue("log succeeds", logger.log(null, null, null, null));
            assertTrue("submit succeeds", logger.submit(null));
            assertTrue("tracing history empty", logger.tracingHistory().size() == 0);
        }
    }

    @Test
    public void submitToGoodUrlTest() {
        HttpLogger logger = new HttpLogger();
        StringBuilder json = new StringBuilder(64);
        JsonMessage.start(json, "echo", logger.getAgent(), logger.getVersion(), System.currentTimeMillis());
        JsonMessage.stop(json);
        assertTrue("submit succeeds", logger.submit(json.toString()));
        assertTrue("tracing history empty", logger.tracingHistory().size() == 0);
    }

    @Test
    public void submitToInvalidUrlTest() {
        for (String url : MOCK_INVALID_URLS) {
            HttpLogger logger = new HttpLogger(url);
            assertTrue("submit fails", !logger.submit("TEST-ABC"));
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
            assertTrue("submit succeeds (1)", logger.submit("TEST-123"));
            assertTrue("tracing history is 1", logger.tracingHistory().size() == 1);
            assertTrue("submit succeeds (2)", logger.submit("TEST-234"));
            assertTrue("tracing history is 2", logger.tracingHistory().size() == 2);
            assertTrue("submit succeeds (3)", logger.submit("TEST-345"));
            assertTrue("tracing history is 3", logger.tracingHistory().size() == 3);
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
        assertEquals(url, new HttpLogger().getUrl());
        assertEquals("https://foobar.com", new HttpLogger("https://foobar.com").getUrl());
    }

    @Test
    public void versionTest() {
        String version = HttpLogger.version_lookup();
        assertTrue("null check", version != null);
        assertTrue("length check", version.length() > 0);
        assertTrue("startsWith check", version.startsWith("1.4."));
        assertTrue("backslash check", !version.contains("\\"));
        assertTrue("double quote check", !version.contains("\""));
        assertTrue("single quote check", !version.contains("'"));
        assertEquals(version, new HttpLogger().getVersion());
    }

}
