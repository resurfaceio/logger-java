// © 2016-2018 Resurface Labs LLC

package io.resurface.tests;

import io.resurface.HttpLogger;
import io.resurface.HttpServletRequestImpl;
import io.resurface.HttpServletResponseImpl;
import org.junit.Test;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static io.resurface.tests.Helper.*;

/**
 * Tests against usage logger for HTTP/HTTPS protocol.
 */
public class HttpLoggerJsonTest {

    private final HttpLogger logger = new HttpLogger("", "include standard");

    @Test
    public void formatRequestTest() {
        String json = logger.format(mockRequest(), mockResponse(), null, null, MOCK_NOW);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("[\"agent\",\"" + logger.getAgent() + "\"]");
        expect(json).toContain("[\"version\",\"" + logger.getVersion() + "\"]");
        expect(json).toContain("[\"now\",\"" + MOCK_NOW + "\"]");
        expect(json).toContain("[\"request_method\",\"GET\"]");
        expect(json).toContain("[\"request_url\",\"" + MOCK_URL + "\"]");
        expect(json.contains("request_body")).toBeFalse();
        expect(json.contains("request_header")).toBeFalse();
        expect(json.contains("request_param")).toBeFalse();
    }

    @Test
    public void formatRequestWithBodyTest() {
        String json = logger.format(mockRequestWithJson(), mockResponse(), null, MOCK_JSON);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("[\"request_body\",\"" + MOCK_JSON_ESCAPED + "\"]");
        expect(json).toContain("[\"request_header:content-type\",\"Application/JSON\"]");
        expect(json).toContain("[\"request_method\",\"POST\"]");
        expect(json).toContain("[\"request_param:message\",\"" + MOCK_JSON_ESCAPED + "\"]");
        expect(json).toContain("[\"request_url\",\"" + MOCK_URL + '?' + MOCK_QUERY_STRING + "\"]");
    }

    @Test
    public void formatRequestWithEmptyBodyTest() {
        String json = logger.format(mockRequestWithJson2(), mockResponse(), null, "");
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("[\"request_header:a\",\"1\"]");
        expect(json).toContain("[\"request_header:a\",\"2\"]");
        expect(json).toContain("[\"request_header:abc\",\"123\"]");
        expect(json).toContain("[\"request_header:content-type\",\"Application/JSON\"]");
        expect(json).toContain("[\"request_method\",\"POST\"]");
        expect(json).toContain("[\"request_param:abc\",\"123\"]");
        expect(json).toContain("[\"request_param:abc\",\"234\"]");
        expect(json).toContain("[\"request_param:message\",\"" + MOCK_JSON_ESCAPED + "\"]");
        expect(json).toContain("[\"request_url\",\"" + MOCK_URL + '?' + MOCK_QUERY_STRING + "\"]");
        expect(json.contains("request_body")).toBeFalse();
    }

    @Test
    public void formatRequestWithMissingDetailsTest() {
        String json = logger.format(new HttpServletRequestImpl(), mockResponse(), null, null, MOCK_NOW);
        expect(parseable(json)).toBeTrue();
        expect(json.contains("request_body")).toBeFalse();
        expect(json.contains("request_header")).toBeFalse();
        expect(json.contains("request_method")).toBeFalse();
        expect(json.contains("request_param")).toBeFalse();
        expect(json.contains("request_url")).toBeFalse();
    }

    @Test
    public void formatResponseTest() {
        String json = logger.format(mockRequest(), mockResponse());
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("[\"response_code\",\"200\"]");
        expect(json.contains("response_body")).toBeFalse();
        expect(json.contains("response_header")).toBeFalse();
    }

    @Test
    public void formatResponseWithBodyTest() {
        String json = logger.format(mockRequest(), mockResponseWithHtml(), MOCK_HTML2);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("[\"response_body\",\"" + MOCK_HTML2 + "\"]");
        expect(json).toContain("[\"response_code\",\"200\"]");
        expect(json).toContain("[\"response_header:content-type\",\"text/html; charset=utf-8\"]");
    }

    @Test
    public void formatResponseWithEmptyBodyTest() {
        String json = logger.format(mockRequest(), mockResponseWithHtml(), "");
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("[\"response_code\",\"200\"]");
        expect(json).toContain("[\"response_header:content-type\",\"text/html; charset=utf-8\"]");
        expect(json.contains("response_body")).toBeFalse();
    }

    @Test
    public void formatResponseWithMissingDetailsTest() {
        String json = logger.format(mockRequest(), new HttpServletResponseImpl());
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("[\"response_code\",\"0\"]");
        expect(json.contains("response_body")).toBeFalse();
        expect(json.contains("response_header")).toBeFalse();
    }
}
