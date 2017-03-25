// © 2016-2017 Resurface Labs LLC

package io.resurface.tests;

import io.resurface.HttpLogger;
import org.junit.Test;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static io.resurface.tests.Helper.*;

/**
 * Tests against usage logger for HTTP/HTTPS protocol.
 */
public class HttpLoggerJsonTest {

    private final HttpLogger logger = new HttpLogger();

    @Test
    public void appendRequestAndResponseTest() {
        String json = logger.appendToBuffer(new StringBuilder(), MOCK_NOW, mockRequest(), null, mockResponse(), null).toString();
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("\"agent\":\"" + HttpLogger.AGENT + "\"");
        expect(json).toContain("\"category\":\"http\"");
        expect(json).toContain("\"version\":\"" + HttpLogger.version_lookup() + "\"");
        expect(json).toContain("\"now\":\"" + MOCK_NOW + "\"");
        expect(json.contains("\"request_body\"")).toBeFalse();
        expect(json).toContain("\"request_headers\":[]");
        expect(json).toContain("\"request_method\":\"GET\"");
        expect(json).toContain("\"request_url\":\"" + MOCK_URL + "\"");
        expect(json.contains("\"response_body\"")).toBeFalse();
        expect(json).toContain("\"response_code\":\"200\"");
        expect(json).toContain("\"response_headers\":[]");
    }

    @Test
    public void formatRequestWithBodyTest() {
        String json = logger.format(mockRequest(), MOCK_JSON, mockResponse(), null);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("\"agent\":\"" + HttpLogger.AGENT + "\"");
        expect(json).toContain("\"category\":\"http\"");
        expect(json).toContain("\"version\":\"" + HttpLogger.version_lookup() + "\"");
        expect(json).toContain("\"request_body\":\"" + MOCK_JSON_ESCAPED + "\"");
        expect(json).toContain("\"request_headers\":[]");
        expect(json).toContain("\"request_method\":\"GET\"");
        expect(json).toContain("\"request_url\":\"" + MOCK_URL + "\"");
    }

    @Test
    public void formatRequestWithEmptyBodyTest() {
        String json = logger.format(mockRequest(), "", mockResponse(), null);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("\"request_body\":\"\"");
        expect(json).toContain("\"request_headers\":[]");
        expect(json).toContain("\"request_method\":\"GET\"");
        expect(json).toContain("\"request_url\":\"" + MOCK_URL + "\"");
    }

    @Test
    public void formatResponseTest() {
        String json = logger.format(mockRequest(), null, mockResponse(), null);
        expect(parseable(json)).toBeTrue();
        expect(json.contains("\"response_body\"")).toBeFalse();
        expect(json).toContain("\"response_code\":\"200\"");
        expect(json).toContain("\"response_headers\":[]");
    }

    @Test
    public void formatResponseWithBodyTest() {
        String json = logger.format(mockRequest(), null, mockResponse(), MOCK_HTML);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("\"response_body\":\"" + MOCK_HTML_ESCAPED + "\"");
        expect(json).toContain("\"response_code\":\"200\"");
        expect(json).toContain("\"response_headers\":[]");
    }

    @Test
    public void formatResponseWithEmptyBodyTest() {
        String json = logger.format(mockRequest(), null, mockResponse(), "");
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("\"response_code\":\"200\"");
        expect(json).toContain("\"response_headers\":[]");
    }

}
