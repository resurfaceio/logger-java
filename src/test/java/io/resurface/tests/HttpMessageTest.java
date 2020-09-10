// © 2016-2020 Resurface Labs Inc.

package io.resurface.tests;

import io.resurface.HttpLogger;
import io.resurface.HttpMessage;
import io.resurface.HttpServletRequestImpl;
import io.resurface.HttpServletResponseImpl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static io.resurface.tests.Helper.*;

/**
 * Tests against usage logger for HTTP/HTTPS protocol.
 */
public class HttpMessageTest {

    @Test
    public void formatMetadataTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "include debug");
        HttpMessage.send(logger, mockRequest(), mockResponse(), null, null, MOCK_NOW, 0);
        expect(queue.size()).toEqual(2);
        String msg = queue.get(0);
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"agent\",\"" + logger.getAgent() + "\"]");
        expect(msg).toContain("[\"host\",\"" + logger.getHost() + "\"]");
        expect(msg).toContain("[\"message_type\",\"metadata\"]");
        expect(msg).toContain("[\"metadata_id\",\"" + logger.getMetadataId() + "\"]");
        expect(msg).toContain("[\"version\",\"" + logger.getVersion() + "\"]");
        expect(msg.contains("[\"interval")).toBeFalse();
        expect(msg.contains("[\"now")).toBeFalse();
        expect(msg.contains("[\"request_")).toBeFalse();
        expect(msg.contains("[\"response_")).toBeFalse();
    }

    @Test
    public void formatRequestTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "include debug");
        HttpMessage.send(logger, mockRequest(), mockResponse(), null, null, MOCK_NOW, 0);
        expect(queue.size()).toEqual(2);
        String msg = queue.get(1);
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"metadata_id\",\"" + logger.getMetadataId() + "\"]");
        expect(msg).toContain("[\"now\",\"" + MOCK_NOW + "\"]");
        expect(msg).toContain("[\"request_method\",\"GET\"]");
        expect(msg).toContain("[\"request_url\",\"" + MOCK_URL + "\"]");
        expect(msg.contains("[\"agent")).toBeFalse();
        expect(msg.contains("[\"host")).toBeFalse();
        expect(msg.contains("[\"interval")).toBeFalse();
        expect(msg.contains("[\"message_type")).toBeFalse();
        expect(msg.contains("[\"request_body")).toBeFalse();
        expect(msg.contains("[\"request_header")).toBeFalse();
        expect(msg.contains("[\"request_param")).toBeFalse();
        expect(msg.contains("[\"version")).toBeFalse();
    }

    @Test
    public void formatRequestWithBodyTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "include debug");
        HttpMessage.send(logger, mockRequestWithJson(), mockResponse(), null, MOCK_HTML);
        expect(queue.size()).toEqual(2);
        String msg = queue.get(1);
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"request_body\",\"" + MOCK_HTML + "\"]");
        expect(msg).toContain("[\"request_header:content-type\",\"Application/JSON\"]");
        expect(msg).toContain("[\"request_method\",\"POST\"]");
        expect(msg).toContain("[\"request_param:message\",\"" + MOCK_JSON_ESCAPED + "\"]");
        expect(msg).toContain("[\"request_url\",\"" + MOCK_URL + '?' + MOCK_QUERY_STRING + "\"]");
        expect(msg.contains("[\"request_param:foo")).toBeFalse();
    }

    @Test
    public void formatRequestWithEmptyBodyTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "include debug");
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponse(), null, "");
        expect(queue.size()).toEqual(2);
        String msg = queue.get(1);
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
        expect(msg.contains("[\"request_body")).toBeFalse();
        expect(msg.contains("[\"request_param:foo")).toBeFalse();
    }

    @Test
    public void formatRequestWithMissingDetailsTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "include debug");
        HttpMessage.send(logger, new HttpServletRequestImpl(), mockResponse(), null, null, MOCK_NOW, 0);
        expect(queue.size()).toEqual(2);
        String msg = queue.get(1);
        expect(parseable(msg)).toBeTrue();
        expect(msg.contains("[\"interval")).toBeFalse();
        expect(msg.contains("[\"request_body")).toBeFalse();
        expect(msg.contains("[\"request_header")).toBeFalse();
        expect(msg.contains("[\"request_method")).toBeFalse();
        expect(msg.contains("[\"request_param")).toBeFalse();
        expect(msg.contains("[\"request_url")).toBeFalse();
    }

    @Test
    public void formatResponseTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "include debug");
        HttpMessage.send(logger, mockRequest(), mockResponse());
        expect(queue.size()).toEqual(2);
        String msg = queue.get(1);
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"response_code\",\"200\"]");
        expect(msg.contains("[\"response_body")).toBeFalse();
        expect(msg.contains("[\"response_header")).toBeFalse();
    }

    @Test
    public void formatResponseWithBodyTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "include debug");
        HttpMessage.send(logger, mockRequest(), mockResponseWithHtml(), MOCK_HTML2);
        expect(queue.size()).toEqual(2);
        String msg = queue.get(1);
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"response_body\",\"" + MOCK_HTML2 + "\"]");
        expect(msg).toContain("[\"response_code\",\"200\"]");
        expect(msg).toContain("[\"response_header:content-type\",\"text/html; charset=utf-8\"]");
    }

    @Test
    public void formatResponseWithEmptyBodyTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "include debug");
        HttpMessage.send(logger, mockRequest(), mockResponseWithHtml(), "");
        expect(queue.size()).toEqual(2);
        String msg = queue.get(1);
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"response_code\",\"200\"]");
        expect(msg).toContain("[\"response_header:content-type\",\"text/html; charset=utf-8\"]");
        expect(msg.contains("[\"response_body")).toBeFalse();
    }

    @Test
    public void formatResponseWithMissingDetailsTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "include debug");
        HttpMessage.send(logger, mockRequest(), new HttpServletResponseImpl());
        expect(queue.size()).toEqual(2);
        String msg = queue.get(1);
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"response_code\",\"0\"]");
        expect(msg.contains("[\"interval")).toBeFalse();
        expect(msg.contains("[\"response_body")).toBeFalse();
        expect(msg.contains("[\"response_header")).toBeFalse();
    }

}
