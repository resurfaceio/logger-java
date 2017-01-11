// © 2016-2017 Resurface Labs LLC

package io.resurface.tests;

import io.resurface.HttpLoggerForServlets;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static io.resurface.tests.Helper.*;
import static org.junit.Assert.assertTrue;
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
        assertTrue("queue size is 1", queue.size() == 1);
        String json = queue.get(0);
        assertTrue("json is valid", parseable(json));
        assertTrue("has category", json.contains("\"category\":\"http\""));
        assertTrue("has request_body", !json.contains("\"request_body\""));
        assertTrue("has request_headers", json.contains("\"request_headers\":[]"));
        assertTrue("has request_method", json.contains("\"request_method\":\"GET\""));
        assertTrue("has request_url", json.contains("\"request_url\":\"" + MOCK_URL + "\""));
        assertTrue("has response_body", json.contains("\"response_body\":\"" + MOCK_HTML_ESCAPED + "\""));
        assertTrue("has response_code", json.contains("\"response_code\":\"404\""));
        assertTrue("has response_headers", json.contains("\"response_headers\":[{\"a\":\"Z\"},{\"content-type\":\"text/html\"}]"));
    }

    @Test
    public void logsJsonTest() throws IOException, ServletException {
        List<String> queue = new ArrayList<>();
        HttpLoggerForServlets filter = new HttpLoggerForServlets(queue);
        filter.init(null);
        filter.doFilter(mockRequest(), mockResponse(), mockJsonApp());
        assertTrue("queue size is 1", queue.size() == 1);
        String json = queue.get(0);
        assertTrue("json is valid", parseable(json));
        assertTrue("has category", json.contains("\"category\":\"http\""));
        assertTrue("has request_body", !json.contains("\"request_body\""));
        assertTrue("has request_headers", json.contains("\"request_headers\":[]"));
        assertTrue("has request_method", json.contains("\"request_method\":\"GET\""));
        assertTrue("has response_body", json.contains("\"response_body\":\"" + MOCK_JSON_ESCAPED + "\""));
        assertTrue("has response_code", json.contains("\"response_code\":\"500\""));
        assertTrue("has response_headers", json.contains("\"response_headers\":[{\"content-type\":\"application/json; charset=utf-8\"}]"));
    }

    @Test
    public void logsJsonPostTest() throws IOException, ServletException {
        List<String> queue = new ArrayList<>();
        HttpLoggerForServlets filter = new HttpLoggerForServlets(queue);
        filter.init(null);
        filter.doFilter(mockRequestWithBody(), mockResponse(), mockJsonApp());
        assertTrue("queue size is 1", queue.size() == 1);
        String json = queue.get(0);
        assertTrue("json is valid", parseable(json));
        assertTrue("has category", json.contains("\"category\":\"http\""));
        assertTrue("has request_body", json.contains("\"request_body\":\"" + MOCK_JSON_ESCAPED + "\""));
        assertTrue("has request_headers", json.contains("\"request_headers\":[{\"content-type\":\"Application/JSON\"}]"));
        assertTrue("has request_method", json.contains("\"request_method\":\"POST\""));
        assertTrue("has request_url", json.contains("\"request_url\":\"" + MOCK_URL + '?' + MOCK_QUERY_STRING + "\""));
        assertTrue("has response_body", json.contains("\"response_body\":\"" + MOCK_JSON_ESCAPED + "\""));
        assertTrue("has response_code", json.contains("\"response_code\":\"500\""));
        assertTrue("has response_headers", json.contains("\"response_headers\":[{\"content-type\":\"application/json; charset=utf-8\"}]"));
    }

    @Test
    public void logsJsonPostWithHeadersTest() throws IOException, ServletException {
        List<String> queue = new ArrayList<>();
        HttpLoggerForServlets filter = new HttpLoggerForServlets(queue);
        filter.init(null);
        filter.doFilter(mockRequestWithBody2(), mockResponse(), mockHtmlApp());
        assertTrue("queue size is 1", queue.size() == 1);
        String json = queue.get(0);
        assertTrue("json is valid", parseable(json));
        assertTrue("has category", json.contains("\"category\":\"http\""));
        assertTrue("has request_body", json.contains("\"request_body\":\"" + MOCK_JSON_ESCAPED + "\""));
        assertTrue("has request_headers", json.contains("\"request_headers\":[{\"a\":\"1\"},{\"a\":\"2\"},{\"content-type\":\"Application/JSON\"}]"));
        assertTrue("has request_method", json.contains("\"request_method\":\"POST\""));
        assertTrue("has request_url", json.contains("\"request_url\":\"" + MOCK_URL + '?' + MOCK_QUERY_STRING + "\""));
        assertTrue("has response_body", json.contains("\"response_body\":\"" + MOCK_HTML_ESCAPED + "\""));
        assertTrue("has response_code", json.contains("\"response_code\":\"404\""));
        assertTrue("has response_headers", json.contains("\"response_headers\":[{\"a\":\"Z\"},{\"content-type\":\"text/html\"}]"));
    }

    @Test
    public void skipsExceptionTest() {
        List<String> queue = new ArrayList<>();
        HttpLoggerForServlets filter = new HttpLoggerForServlets(queue);
        filter.init(null);
        try {
            filter.doFilter(mockRequest(), mockResponse(), mockExceptionApp());
        } catch (UnsupportedEncodingException uee) {
            assertTrue("queue size is 0", queue.size() == 0);
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
        assertTrue("queue size is 0", queue.size() == 0);
    }

}
