// © 2016-2019 Resurface Labs Inc.

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
        String msg = logger.format(mockRequest(), mockResponse(), null, null, MOCK_NOW);
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"agent\",\"" + logger.getAgent() + "\"]");
        expect(msg).toContain("[\"host\",\"" + logger.getHost() + "\"]");
        expect(msg).toContain("[\"version\",\"" + logger.getVersion() + "\"]");
        expect(msg).toContain("[\"now\",\"" + MOCK_NOW + "\"]");
        expect(msg).toContain("[\"request_method\",\"GET\"]");
        expect(msg).toContain("[\"request_url\",\"" + MOCK_URL + "\"]");
        expect(msg.contains("request_body")).toBeFalse();
        expect(msg.contains("request_header")).toBeFalse();
        expect(msg.contains("request_param")).toBeFalse();
    }

    @Test
    public void formatRequestWithBodyTest() {
        String msg = logger.format(mockRequestWithJson(), mockResponse(), null, MOCK_HTML);
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"request_body\",\"" + MOCK_HTML + "\"]");
        expect(msg).toContain("[\"request_header:content-type\",\"Application/JSON\"]");
        expect(msg).toContain("[\"request_method\",\"POST\"]");
        expect(msg).toContain("[\"request_param:message\",\"" + MOCK_JSON_ESCAPED + "\"]");
        expect(msg).toContain("[\"request_url\",\"" + MOCK_URL + '?' + MOCK_QUERY_STRING + "\"]");
        expect(msg.contains("request_param:foo")).toBeFalse();
    }

    @Test
    public void formatRequestWithEmptyBodyTest() {
        String msg = logger.format(mockRequestWithJson2(), mockResponse(), null, "");
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"request_header:a\",\"1\"]");
        expect(msg).toContain("[\"request_header:a\",\"2\"]");
        expect(msg).toContain("[\"request_header:abc\",\"123\"]");
        expect(msg).toContain("[\"request_header:content-type\",\"Application/JSON\"]");
        expect(msg).toContain("[\"request_method\",\"POST\"]");
        expect(msg).toContain("[\"request_param:abc\",\"123\"]");
        expect(msg).toContain("[\"request_param:abc\",\"234\"]");
        expect(msg).toContain("[\"request_param:message\",\"" + MOCK_JSON_ESCAPED + "\"]");
        expect(msg).toContain("[\"request_url\",\"" + MOCK_URL + '?' + MOCK_QUERY_STRING + "\"]");
        expect(msg.contains("request_body")).toBeFalse();
        expect(msg.contains("request_param:foo")).toBeFalse();
    }

    @Test
    public void formatRequestWithMissingDetailsTest() {
        String msg = logger.format(new HttpServletRequestImpl(), mockResponse(), null, null, MOCK_NOW);
        expect(parseable(msg)).toBeTrue();
        expect(msg.contains("request_body")).toBeFalse();
        expect(msg.contains("request_header")).toBeFalse();
        expect(msg.contains("request_method")).toBeFalse();
        expect(msg.contains("request_param")).toBeFalse();
        expect(msg.contains("request_url")).toBeFalse();
    }

    @Test
    public void formatResponseTest() {
        String msg = logger.format(mockRequest(), mockResponse());
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"response_code\",\"200\"]");
        expect(msg.contains("response_body")).toBeFalse();
        expect(msg.contains("response_header")).toBeFalse();
    }

    @Test
    public void formatResponseWithBodyTest() {
        String msg = logger.format(mockRequest(), mockResponseWithHtml(), MOCK_HTML2);
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"response_body\",\"" + MOCK_HTML2 + "\"]");
        expect(msg).toContain("[\"response_code\",\"200\"]");
        expect(msg).toContain("[\"response_header:content-type\",\"text/html; charset=utf-8\"]");
    }

    @Test
    public void formatResponseWithEmptyBodyTest() {
        String msg = logger.format(mockRequest(), mockResponseWithHtml(), "");
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"response_code\",\"200\"]");
        expect(msg).toContain("[\"response_header:content-type\",\"text/html; charset=utf-8\"]");
        expect(msg.contains("response_body")).toBeFalse();
    }

    @Test
    public void formatResponseWithMissingDetailsTest() {
        String msg = logger.format(mockRequest(), new HttpServletResponseImpl());
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"response_code\",\"0\"]");
        expect(msg.contains("response_body")).toBeFalse();
        expect(msg.contains("response_header")).toBeFalse();
    }
}
