// © 2016-2019 Resurface Labs Inc.

package io.resurface.tests;

import io.resurface.HttpLoggerForServlets;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static io.resurface.tests.Helper.*;
import static org.junit.Assert.fail;

/**
 * Tests against servlet filter for HTTP usage logging.
 */
public class HttpLoggerForServletsTest {

    @Test
    public void logsHtmlTest() throws IOException, ServletException {
        List<String> queue = new ArrayList<>();
        HttpLoggerForServlets filter = new HttpLoggerForServlets(queue, "include standard");
        filter.init(null);
        filter.doFilter(mockRequest(), mockResponse(), mockHtmlApp());
        expect(queue.size()).toEqual(1);
        String msg = queue.get(0);
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"request_method\",\"GET\"]");
        expect(msg).toContain("[\"request_url\",\"" + MOCK_URL + "\"]");
        expect(msg).toContain("[\"response_body\",\"" + MOCK_HTML + "\"]");
        expect(msg).toContain("[\"response_code\",\"200\"]");
        expect(msg).toContain("[\"response_header:a\",\"Z\"]");
        expect(msg).toContain("[\"response_header:content-type\",\"text/html\"]");
        expect(msg.contains("request_body")).toBeFalse();
        expect(msg.contains("request_header")).toBeFalse();
        expect(msg.contains("request_param")).toBeFalse();
    }

    @Test
    public void logsJsonTest() throws IOException, ServletException {
        List<String> queue = new ArrayList<>();
        HttpLoggerForServlets filter = new HttpLoggerForServlets(queue, "include standard");
        filter.init(null);
        filter.doFilter(mockRequest(), mockResponse(), mockJsonApp());
        expect(queue.size()).toEqual(1);
        String msg = queue.get(0);
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"request_method\",\"GET\"]");
        expect(msg).toContain("[\"response_body\",\"" + MOCK_JSON_ESCAPED + "\"]");
        expect(msg).toContain("[\"response_code\",\"200\"]");
        expect(msg).toContain("[\"response_header:content-type\",\"application/json; charset=utf-8\"]");
        expect(msg.contains("request_body")).toBeFalse();
        expect(msg.contains("request_header")).toBeFalse();
        expect(msg.contains("request_param")).toBeFalse();
    }

    @Test
    public void logsJsonPostTest() throws IOException, ServletException {
        List<String> queue = new ArrayList<>();
        HttpLoggerForServlets filter = new HttpLoggerForServlets(queue, "include standard");
        filter.init(null);
        filter.doFilter(mockRequestWithJson(), mockResponse(), mockJsonApp());
        expect(queue.size()).toEqual(1);
        String msg = queue.get(0);
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"request_header:content-type\",\"Application/JSON\"]");
        expect(msg).toContain("[\"request_method\",\"POST\"]");
        expect(msg).toContain("[\"request_param:message\",\"" + MOCK_JSON_ESCAPED + "\"]");
        expect(msg).toContain("[\"request_url\",\"" + MOCK_URL + '?' + MOCK_QUERY_STRING + "\"]");
        expect(msg).toContain("[\"response_body\",\"" + MOCK_JSON_ESCAPED + "\"]");
        expect(msg).toContain("[\"response_code\",\"200\"]");
        expect(msg).toContain("[\"response_header:content-type\",\"application/json; charset=utf-8\"]");
    }

    @Test
    public void logsJsonPostWithHeadersTest() throws IOException, ServletException {
        List<String> queue = new ArrayList<>();
        HttpLoggerForServlets filter = new HttpLoggerForServlets(queue, "include standard");
        filter.init(null);
        filter.doFilter(mockRequestWithJson2(), mockResponse(), mockHtmlApp());
        expect(queue.size()).toEqual(1);
        String msg = queue.get(0);
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"request_header:a\",\"1\"]");
        expect(msg).toContain("[\"request_header:a\",\"2\"]");
        expect(msg).toContain("[\"request_header:content-type\",\"Application/JSON\"]");
        expect(msg).toContain("[\"request_method\",\"POST\"]");
        expect(msg).toContain("[\"request_param:abc\",\"123\"]");
        expect(msg).toContain("[\"request_param:abc\",\"234\"]");
        expect(msg).toContain("[\"request_param:message\",\"" + MOCK_JSON_ESCAPED + "\"]");
        expect(msg).toContain("[\"request_url\",\"" + MOCK_URL + '?' + MOCK_QUERY_STRING + "\"]");
        expect(msg).toContain("[\"response_body\",\"" + MOCK_HTML + "\"]");
        expect(msg).toContain("[\"response_code\",\"200\"]");
        expect(msg).toContain("[\"response_header:a\",\"Z\"]");
        expect(msg).toContain("[\"response_header:content-type\",\"text/html\"]");
    }

    @Test
    public void skipsExceptionTest() {
        List<String> queue = new ArrayList<>();
        HttpLoggerForServlets filter = new HttpLoggerForServlets(queue, "include standard");
        filter.init(null);
        try {
            filter.doFilter(mockRequest(), mockResponse(), mockExceptionApp());
        } catch (UnsupportedEncodingException uee) {
            expect(queue.size()).toEqual(0);
        } catch (Exception e) {
            fail("Unexpected exception type");
        }
    }

    @Test
    public void skipsLoggingTest() throws IOException, ServletException {
        List<String> queue = new ArrayList<>();
        HttpLoggerForServlets filter = new HttpLoggerForServlets(queue, "include standard");
        filter.init(null);
        filter.doFilter(mockRequest(), mockResponse(), mockCustomApp());
        filter.doFilter(mockRequest(), mockResponse(), mockCustom404App());
        filter.doFilter(mockRequest(), mockResponse(), mockHtml404App());
        filter.doFilter(mockRequest(), mockResponse(), mockJson404App());
        expect(queue.size()).toEqual(0);
    }

}
