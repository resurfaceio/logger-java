// © 2016-2017 Resurface Labs LLC

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
        HttpLoggerForServlets filter = new HttpLoggerForServlets(queue);
        filter.init(null);
        filter.doFilter(mockRequest(), mockResponse(), mockHtmlApp());
        expect(queue.size()).toEqual(1);
        String json = queue.get(0);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("\"category\":\"http\"");
        expect(json.contains("\"request_body\"")).toBeFalse();
        expect(json).toContain("\"request_headers\":[]");
        expect(json).toContain("\"request_method\":\"GET\"");
        expect(json).toContain("\"request_url\":\"" + MOCK_URL + "\"");
        expect(json).toContain("\"response_body\":\"" + MOCK_HTML_ESCAPED + "\"");
        expect(json).toContain("\"response_code\":\"404\"");
        expect(json).toContain("\"response_headers\":[{\"a\":\"Z\"},{\"content-type\":\"text/html\"}]");
    }

    @Test
    public void logsJsonTest() throws IOException, ServletException {
        List<String> queue = new ArrayList<>();
        HttpLoggerForServlets filter = new HttpLoggerForServlets(queue);
        filter.init(null);
        filter.doFilter(mockRequest(), mockResponse(), mockJsonApp());
        expect(queue.size()).toEqual(1);
        String json = queue.get(0);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("\"category\":\"http\"");
        expect(json.contains("\"request_body\"")).toBeFalse();
        expect(json).toContain("\"request_headers\":[]");
        expect(json).toContain("\"request_method\":\"GET\"");
        expect(json).toContain("\"response_body\":\"" + MOCK_JSON_ESCAPED + "\"");
        expect(json).toContain("\"response_code\":\"500\"");
        expect(json).toContain("\"response_headers\":[{\"content-type\":\"application/json; charset=utf-8\"}]");
    }

    @Test
    public void logsJsonPostTest() throws IOException, ServletException {
        List<String> queue = new ArrayList<>();
        HttpLoggerForServlets filter = new HttpLoggerForServlets(queue);
        filter.init(null);
        filter.doFilter(mockRequestWithBody(), mockResponse(), mockJsonApp());
        expect(queue.size()).toEqual(1);
        String json = queue.get(0);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("\"category\":\"http\"");
        expect(json).toContain("\"response_body\":\"" + MOCK_JSON_ESCAPED + "\"");
        expect(json).toContain("\"request_headers\":[{\"content-type\":\"Application/JSON\"}]");
        expect(json).toContain("\"request_method\":\"POST\"");
        expect(json).toContain("\"request_url\":\"" + MOCK_URL + '?' + MOCK_QUERY_STRING + "\"");
        expect(json).toContain("\"response_body\":\"" + MOCK_JSON_ESCAPED + "\"");
        expect(json).toContain("\"response_code\":\"500\"");
        expect(json).toContain("\"response_headers\":[{\"content-type\":\"application/json; charset=utf-8\"}]");
    }

    @Test
    public void logsJsonPostWithHeadersTest() throws IOException, ServletException {
        List<String> queue = new ArrayList<>();
        HttpLoggerForServlets filter = new HttpLoggerForServlets(queue);
        filter.init(null);
        filter.doFilter(mockRequestWithBody2(), mockResponse(), mockHtmlApp());
        expect(queue.size()).toEqual(1);
        String json = queue.get(0);
        expect(parseable(json)).toBeTrue();
        expect(json).toContain("\"category\":\"http\"");
        expect(json).toContain("\"request_body\":\"" + MOCK_JSON_ESCAPED + "\"");
        expect(json).toContain("\"request_headers\":[{\"a\":\"1\"},{\"a\":\"2\"},{\"content-type\":\"Application/JSON\"}]");
        expect(json).toContain("\"request_method\":\"POST\"");
        expect(json).toContain("\"request_url\":\"" + MOCK_URL + '?' + MOCK_QUERY_STRING + "\"");
        expect(json).toContain("\"response_body\":\"" + MOCK_HTML_ESCAPED + "\"");
        expect(json).toContain("\"response_code\":\"404\"");
        expect(json).toContain("\"response_headers\":[{\"a\":\"Z\"},{\"content-type\":\"text/html\"}]");
    }

    @Test
    public void skipsExceptionTest() {
        List<String> queue = new ArrayList<>();
        HttpLoggerForServlets filter = new HttpLoggerForServlets(queue);
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
        HttpLoggerForServlets filter = new HttpLoggerForServlets(queue);
        filter.init(null);
        filter.doFilter(mockRequest(), mockResponse(), mockCustomApp());
        filter.doFilter(mockRequest(), mockResponse(), mockCustomRedirectApp());
        filter.doFilter(mockRequest(), mockResponse(), mockHtmlRedirectApp());
        filter.doFilter(mockRequest(), mockResponse(), mockJsonRedirectApp());
        expect(queue.size()).toEqual(0);
    }

}
