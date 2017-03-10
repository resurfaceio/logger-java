// © 2016-2017 Resurface Labs LLC

package io.resurface.tests;

import io.resurface.HttpLogger;
import io.resurface.UsageLoggers;
import org.junit.Test;

import static io.resurface.tests.Helper.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests against usage logger for HTTP/HTTPS protocol.
 */
public class HttpLoggerTest {

    private final HttpLogger logger = new HttpLogger();

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
    public void managesMultipleInstancesTest() {
        String url1 = "http://resurface.io";
        String url2 = "http://whatever.com";
        HttpLogger logger1 = new HttpLogger(url1);
        HttpLogger logger2 = new HttpLogger(url2);
        HttpLogger logger3 = new HttpLogger("DEMO");

        assertEquals(logger1.getAgent(), HttpLogger.AGENT);
        assertEquals(logger1.isEnabled(), true);
        assertEquals(logger1.getUrl(), url1);
        assertEquals(logger2.getAgent(), HttpLogger.AGENT);
        assertEquals(logger2.isEnabled(), true);
        assertEquals(logger2.getUrl(), url2);
        assertEquals(logger3.getAgent(), HttpLogger.AGENT);
        assertEquals(logger3.isEnabled(), true);
        assertEquals(logger3.getUrl(), UsageLoggers.urlForDemo());

        UsageLoggers.disable();
        assertEquals(UsageLoggers.isEnabled(), false);
        assertEquals(logger1.isEnabled(), false);
        assertEquals(logger2.isEnabled(), false);
        assertEquals(logger3.isEnabled(), false);
        UsageLoggers.enable();
        assertEquals(UsageLoggers.isEnabled(), true);
        assertEquals(logger1.isEnabled(), true);
        assertEquals(logger2.isEnabled(), true);
        assertEquals(logger3.isEnabled(), true);
    }

    @Test
    public void providesValidAgentTest() {
        String agent = HttpLogger.AGENT;
        assertTrue("length check", agent.length() > 0);
        assertTrue("endsWith check", agent.endsWith(".java"));
        assertTrue("backslash check", !agent.contains("\\"));
        assertTrue("double quote check", !agent.contains("\""));
        assertTrue("single quote check", !agent.contains("'"));
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

}
