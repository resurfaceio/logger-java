// © 2016-2017 Resurface Labs LLC

package io.resurface.tests;

import io.resurface.HttpLogger;
import io.resurface.JsonMessage;
import io.resurface.UsageLoggers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
    public void performsEnablingWhenExpectedTest() {
        HttpLogger logger = new HttpLogger("DEMO", false);
        assertTrue("logger disabled", !logger.isEnabled());
        assertTrue("url matches", UsageLoggers.urlForDemo().equals(logger.getUrl()));
        logger.enable();
        assertTrue("logger enabled", logger.isEnabled());

        List<String> queue = new ArrayList<>();
        logger = new HttpLogger(queue, false);
        assertTrue("logger disabled", !logger.isEnabled());
        assertTrue("url is null", logger.getUrl() == null);
        logger.enable().disable().enable();
        assertTrue("logger enabled", logger.isEnabled());

        logger = new HttpLogger(UsageLoggers.urlForDemo(), false);
        assertTrue("logger disabled", !logger.isEnabled());
        assertTrue("url matches", UsageLoggers.urlForDemo().equals(logger.getUrl()));
        logger.enable().disable().enable().disable().disable().disable().enable();
        assertTrue("logger enabled", logger.isEnabled());
    }

    @Test
    public void skipsEnablingForInvalidUrlsTest() {
        for (String url : URLS_INVALID) {
            HttpLogger logger = new HttpLogger(url);
            assertTrue("logger disabled at first", !logger.isEnabled());
            assertTrue("url is null", logger.getUrl() == null);
            logger.enable();
            assertTrue("logger still disabled", !logger.isEnabled());
        }
    }

    @Test
    public void skipsEnablingForNullUrlTest() {
        String url = null;
        HttpLogger logger = new HttpLogger(url);
        assertTrue("logger disabled at first", !logger.isEnabled());
        assertTrue("url is null", logger.getUrl() == null);
        logger.enable();
        assertTrue("logger still disabled", !logger.isEnabled());
    }

    @Test
    public void skipsLoggingWhenDisabledTest() {
        for (String url : URLS_DENIED) {
            HttpLogger logger = new HttpLogger(url).disable();
            assertTrue("logger disabled", !logger.isEnabled());
            assertTrue("url matches", url.equals(logger.getUrl()));
            assertTrue("log succeeds", logger.log(null, null, null, null));    // would fail if enabled
            assertTrue("submit succeeds", logger.submit(null));                // would fail if enabled
        }
    }

    @Test
    public void submitToDemoUrlTest() {
        HttpLogger logger = new HttpLogger(UsageLoggers.urlForDemo());
        assertTrue("url matches", UsageLoggers.urlForDemo().equals(logger.getUrl()));
        StringBuilder json = new StringBuilder(64);
        JsonMessage.start(json, "echo", logger.getAgent(), logger.getVersion(), System.currentTimeMillis());
        JsonMessage.stop(json);
        assertTrue("submit succeeds", logger.submit(json.toString()));
    }

    @Test
    public void submitToDemoUrlViaHttpTest() {
        HttpLogger logger = new HttpLogger(UsageLoggers.urlForDemo().replace("https://", "http://"));
        assertTrue("url matches", logger.getUrl().contains("http://"));
        StringBuilder json = new StringBuilder(64);
        JsonMessage.start(json, "echo", logger.getAgent(), logger.getVersion(), System.currentTimeMillis());
        JsonMessage.stop(json);
        assertTrue("submit succeeds", logger.submit(json.toString()));
    }

    @Test
    public void submitToDeniedUrlAndFailsTest() {
        for (String url : URLS_DENIED) {
            HttpLogger logger = new HttpLogger(url);
            assertTrue("url matches", url.equals(logger.getUrl()));
            assertTrue("logger enabled", logger.isEnabled());
            assertTrue("submit fails", !logger.submit("TEST-ABC"));
        }
    }

    @Test
    public void submitToQueueTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue);
        assertTrue("url is null", logger.getUrl() == null);
        assertTrue("logger enabled", logger.isEnabled());
        assertTrue("queue size is 0", queue.size() == 0);
        assertTrue("submit succeeds", logger.submit("TEST-123"));
        assertTrue("queue size is 1", queue.size() == 1);
        assertTrue("submit succeeds", logger.submit("TEST-234"));
        assertTrue("queue size is 2", queue.size() == 2);
    }

    @Test
    public void versionTest() {
        String version = HttpLogger.version_lookup();
        assertTrue("null check", version != null);
        assertTrue("length check", version.length() > 0);
        assertTrue("startsWith check", version.startsWith("1.6."));
        assertTrue("backslash check", !version.contains("\\"));
        assertTrue("double quote check", !version.contains("\""));
        assertTrue("single quote check", !version.contains("'"));
        assertEquals(version, new HttpLogger().getVersion());
    }

}
