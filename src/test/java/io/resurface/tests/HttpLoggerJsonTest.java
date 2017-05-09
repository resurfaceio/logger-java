// © 2016-2017 Resurface Labs LLC

package io.resurface.tests;

import io.resurface.HttpLogger;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static io.resurface.tests.Helper.*;

/**
 * Tests against usage logger for HTTP/HTTPS protocol.
 */
public class HttpLoggerJsonTest {

    private final HttpLogger logger = new HttpLogger();

    @Test
    public void formatRequestTest() {
        String json = logger.format(mockRequest(), null, mockResponse(), null, MOCK_NOW);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("[\"agent\",\"" + logger.getAgent() + "\"]");
        expect(json).toContain("[\"version\",\"" + logger.getVersion() + "\"]");
        expect(json).toContain("[\"now\",\"" + MOCK_NOW + "\"]");
        expect(json).toContain("[\"request_method\",\"GET\"]");
        expect(json).toContain("[\"request_url\",\"" + MOCK_URL + "\"]");
        expect(json.contains("request_body")).toBeFalse();
        expect(json.contains("request_header")).toBeFalse();
        expect(json.contains("response_body")).toBeFalse();
        expect(json.contains("response_header")).toBeFalse();
    }

    @Test
    public void formatRequestWithBodyTest() throws UnsupportedEncodingException {
        String json = logger.format(mockRequestWithBody(), MOCK_JSON, mockResponse(), null);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("[\"request_body\",\"" + MOCK_JSON_ESCAPED + "\"]");
        expect(json).toContain("[\"request_header.content-type\",\"Application/JSON\"]");
        expect(json).toContain("[\"request_method\",\"POST\"]");
        expect(json).toContain("[\"request_url\",\"" + MOCK_URL + '?' + MOCK_QUERY_STRING + "\"]");
    }

    @Test
    public void formatRequestWithEmptyBodyTest() throws UnsupportedEncodingException {
        String json = logger.format(mockRequestWithBody2(), "", mockResponse(), null);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("[\"request_body\",\"\"]");
        expect(json).toContain("[\"request_header.a\",\"1\"]");
        expect(json).toContain("[\"request_header.a\",\"2\"]");
        expect(json).toContain("[\"request_header.abc\",\"123\"]");
        expect(json).toContain("[\"request_header.content-type\",\"Application/JSON\"]");
        expect(json).toContain("[\"request_method\",\"POST\"]");
        expect(json).toContain("[\"request_url\",\"" + MOCK_URL + '?' + MOCK_QUERY_STRING + "\"]");
    }

    @Test
    public void formatResponseTest() {
        String json = logger.format(mockRequest(), null, mockResponse(), null);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("[\"response_code\",\"200\"]");
        expect(json.contains("response_body")).toBeFalse();
        expect(json.contains("response_header")).toBeFalse();
    }

    @Test
    public void formatResponseWithBodyTest() {
        String json = logger.format(mockRequest(), null, mockResponseWithBody(), MOCK_HTML);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("[\"response_body\",\"" + MOCK_HTML + "\"]");
        expect(json).toContain("[\"response_code\",\"200\"]");
        expect(json).toContain("[\"response_header.content-type\",\"text/html; charset=utf-8\"]");
    }

    @Test
    public void formatResponseWithEmptyBodyTest() {
        String json = logger.format(mockRequest(), null, mockResponseWithBody(), "");
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("[\"response_body\",\"\"]");
        expect(json).toContain("[\"response_code\",\"200\"]");
        expect(json).toContain("[\"response_header.content-type\",\"text/html; charset=utf-8\"]");
    }

}
