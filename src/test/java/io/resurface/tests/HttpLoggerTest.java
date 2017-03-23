// © 2016-2017 Resurface Labs LLC

package io.resurface.tests;

import io.resurface.HttpLogger;
import io.resurface.UsageLoggers;
import org.junit.Test;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static io.resurface.tests.Helper.*;

/**
 * Tests against usage logger for HTTP/HTTPS protocol.
 */
public class HttpLoggerTest {

    private final HttpLogger logger = new HttpLogger();

    @Test
    public void appendRequestTest() {
        String json = logger.appendToBuffer(new StringBuilder(), MOCK_NOW, mockRequest(), null, mockResponse(), null).toString();
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("\"agent\":\"" + HttpLogger.AGENT + "\"");
        expect(json).toContain("\"category\":\"http\"");
        expect(json).toContain("\"now\":\"" + MOCK_NOW + "\"");
        expect(json.contains("\"request_body\"")).toBeFalse();
        expect(json).toContain("\"request_headers\":[]");
        expect(json).toContain("\"request_method\":\"GET\"");
        expect(json).toContain("\"request_url\":\"" + MOCK_URL + "\"");
        expect(json).toContain("\"version\":\"" + HttpLogger.version_lookup() + "\"");
    }

    @Test
    public void formatRequestWithBodyTest() {
        String json = logger.format(mockRequest(), MOCK_JSON, mockResponse(), null);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("\"agent\":\"" + HttpLogger.AGENT + "\"");
        expect(json).toContain("\"category\":\"http\"");
        expect(json).toContain("\"request_body\":\"" + MOCK_JSON_ESCAPED + "\"");
        expect(json).toContain("\"request_headers\":[]");
        expect(json).toContain("\"request_method\":\"GET\"");
        expect(json).toContain("\"request_url\":\"" + MOCK_URL + "\"");
        expect(json).toContain("\"version\":\"" + HttpLogger.version_lookup() + "\"");
    }

    @Test
    public void formatRequestWithEmptyBodyTest() {
        String json = logger.format(mockRequest(), "", mockResponse(), null);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("\"agent\":\"" + HttpLogger.AGENT + "\"");
        expect(json).toContain("\"category\":\"http\"");
        expect(json).toContain("\"request_body\":\"\"");
        expect(json).toContain("\"request_headers\":[]");
        expect(json).toContain("\"request_method\":\"GET\"");
        expect(json).toContain("\"request_url\":\"" + MOCK_URL + "\"");
        expect(json).toContain("\"version\":\"" + HttpLogger.version_lookup() + "\"");
    }

    @Test
    public void formatResponseTest() {
        String json = logger.format(mockRequest(), null, mockResponse(), null);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("\"agent\":\"" + HttpLogger.AGENT + "\"");
        expect(json).toContain("\"category\":\"http\"");
        expect(json.contains("\"response_body\"")).toBeFalse();
        expect(json).toContain("\"response_code\":\"200\"");
        expect(json).toContain("\"response_headers\":[]");
        expect(json).toContain("\"version\":\"" + HttpLogger.version_lookup() + "\"");
    }

    @Test
    public void formatResponseWithBodyTest() {
        String json = logger.format(mockRequest(), null, mockResponse(), MOCK_HTML);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("\"agent\":\"" + HttpLogger.AGENT + "\"");
        expect(json).toContain("\"category\":\"http\"");
        expect(json).toContain("\"response_body\":\"" + MOCK_HTML_ESCAPED + "\"");
        expect(json).toContain("\"response_code\":\"200\"");
        expect(json).toContain("\"response_headers\":[]");
        expect(json).toContain("\"version\":\"" + HttpLogger.version_lookup() + "\"");
    }

    @Test
    public void formatResponseWithEmptyBodyTest() {
        String json = logger.format(mockRequest(), null, mockResponse(), "");
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("\"agent\":\"" + HttpLogger.AGENT + "\"");
        expect(json).toContain("\"category\":\"http\"");
        expect(json).toContain("\"response_body\":\"\"");
        expect(json).toContain("\"response_code\":\"200\"");
        expect(json).toContain("\"response_headers\":[]");
        expect(json).toContain("\"version\":\"" + HttpLogger.version_lookup() + "\"");
    }

    @Test
    public void managesMultipleInstancesTest() {
        String url1 = "http://resurface.io";
        String url2 = "http://whatever.com";
        HttpLogger logger1 = new HttpLogger(url1);
        HttpLogger logger2 = new HttpLogger(url2);
        HttpLogger logger3 = new HttpLogger("DEMO");

        expect(logger1.getAgent()).toEqual(HttpLogger.AGENT);
        expect(logger1.isEnabled()).toBeTrue();
        expect(logger1.getUrl()).toEqual(url1);
        expect(logger2.getAgent()).toEqual(HttpLogger.AGENT);
        expect(logger2.isEnabled()).toBeTrue();
        expect(logger2.getUrl()).toEqual(url2);
        expect(logger3.getAgent()).toEqual(HttpLogger.AGENT);
        expect(logger3.isEnabled()).toBeTrue();
        expect(logger3.getUrl()).toEqual(UsageLoggers.urlForDemo());

        UsageLoggers.disable();
        expect(UsageLoggers.isEnabled()).toBeFalse();
        expect(logger1.isEnabled()).toBeFalse();
        expect(logger2.isEnabled()).toBeFalse();
        expect(logger3.isEnabled()).toBeFalse();
        UsageLoggers.enable();
        expect(UsageLoggers.isEnabled()).toBeTrue();
        expect(logger1.isEnabled()).toBeTrue();
        expect(logger2.isEnabled()).toBeTrue();
        expect(logger3.isEnabled()).toBeTrue();
    }

    @Test
    public void providesValidAgentTest() {
        String agent = HttpLogger.AGENT;
        expect(agent.length()).toBeGreaterThan(0);
        expect(agent).toEndWith(".java");
        expect(agent.contains("\\")).toBeFalse();
        expect(agent.contains("\"")).toBeFalse();
        expect(agent.contains("'")).toBeFalse();
        expect(new HttpLogger().getAgent()).toEqual(agent);
    }

    @Test
    public void skipsLoggingWhenDisabledTest() {
        for (String url : URLS_DENIED) {
            HttpLogger logger = new HttpLogger(url).disable();
            expect(logger.isEnabled()).toBeFalse();
            expect(logger.log(null, null, null, null)).toBeTrue();  // would fail if enabled
        }
    }

}
