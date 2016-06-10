// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface.tests;

import io.resurface.HttpLogger;
import io.resurface.HttpLoggerFactory;
import io.resurface.HttpLoggerForServlets;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

import static io.resurface.tests.Helper.*;
import static org.junit.Assert.assertTrue;

/**
 * Tests against servlet filter for HTTP usage logging.
 */
public class HttpLoggerForServletsTest {

    @Test
    public void logsHtmlTest() throws IOException, ServletException {
        HttpLogger logger = HttpLoggerFactory.get().disable().tracingStart();
        try {
            HttpLoggerForServlets filter = new HttpLoggerForServlets();
            filter.init(null);
            filter.doFilter(mockRequest(), mockResponse(), mockHtmlApp());
            assertTrue("tracing history is 2", logger.tracingHistory().size() == 2);
            String json = logger.tracingHistory().get(0);
            assertTrue("json is valid", parseable(json));
            assertTrue("has category", json.contains("\"category\":\"http_request\""));
            assertTrue("has request_body", !json.contains("\"request_body\""));
            assertTrue("has request_headers", json.contains("\"request_headers\":[]"));
            assertTrue("has request_method", json.contains("\"request_method\":\"GET\""));
            assertTrue("has request_url", json.contains("\"request_url\":\"" + MOCK_URL + "\""));
            json = logger.tracingHistory().get(1);
            assertTrue("json is valid", parseable(json));
            assertTrue("has category", json.contains("\"category\":\"http_response\""));
            assertTrue("has response_body", json.contains("\"response_body\":\"" + MOCK_HTML_ESCAPED + "\""));
            assertTrue("has response_code", json.contains("\"response_code\":\"404\""));
            assertTrue("has response_headers",
                    json.contains("\"response_headers\":[{\"a\":\"Z\"},{\"content-type\":\"text/html\"}]"));
        } finally {
            logger.tracingStop().enable();
        }
    }

    @Test
    public void logsJsonTest() throws IOException, ServletException {
        HttpLogger logger = HttpLoggerFactory.get().disable().tracingStart();
        try {
            HttpLoggerForServlets filter = new HttpLoggerForServlets();
            filter.init(null);
            filter.doFilter(mockRequest(), mockResponse(), mockJsonApp());
            assertTrue("tracing history is 2", logger.tracingHistory().size() == 2);
            String json = logger.tracingHistory().get(0);
            assertTrue("json is valid", parseable(json));
            assertTrue("has category", json.contains("\"category\":\"http_request\""));
            assertTrue("has request_body", !json.contains("\"request_body\""));
            assertTrue("has request_headers", json.contains("\"request_headers\":[]"));
            assertTrue("has request_method", json.contains("\"request_method\":\"GET\""));
            assertTrue("has request_url", json.contains("\"request_url\":\"" + MOCK_URL + "\""));
            json = logger.tracingHistory().get(1);
            assertTrue("json is valid", parseable(json));
            assertTrue("has category", json.contains("\"category\":\"http_response\""));
            assertTrue("has response_body", json.contains("\"response_body\":\"" + MOCK_JSON_ESCAPED + "\""));
            assertTrue("has response_code", json.contains("\"response_code\":\"500\""));
            assertTrue("has response_headers", json.contains("\"response_headers\":[{\"content-type\":\"application/json\"}]"));
        } finally {
            logger.tracingStop().enable();
        }
    }

    @Test
    public void logsJsonPostTest() throws IOException, ServletException {
        HttpLogger logger = HttpLoggerFactory.get().disable().tracingStart();
        try {
            HttpLoggerForServlets filter = new HttpLoggerForServlets();
            filter.init(null);
            filter.doFilter(mockRequestWithBody(), mockResponse(), mockJsonApp());
            assertTrue("tracing history is 2", logger.tracingHistory().size() == 2);
            String json = logger.tracingHistory().get(0);
            assertTrue("json is valid", parseable(json));
            assertTrue("has category", json.contains("\"category\":\"http_request\""));
            assertTrue("has request_body", json.contains("\"request_body\":\"" + MOCK_JSON_ESCAPED + "\""));
            assertTrue("has request_headers", json.contains("\"request_headers\":[{\"content-type\":\"application/json\"}]"));
            assertTrue("has request_method", json.contains("\"request_method\":\"POST\""));
            assertTrue("has request_url", json.contains("\"request_url\":\"" + MOCK_URL + '?' + MOCK_QUERY_STRING + "\""));
            json = logger.tracingHistory().get(1);
            assertTrue("json is valid", parseable(json));
            assertTrue("has category", json.contains("\"category\":\"http_response\""));
            assertTrue("has response_body", json.contains("\"response_body\":\"" + MOCK_JSON_ESCAPED + "\""));
            assertTrue("has response_code", json.contains("\"response_code\":\"500\""));
            assertTrue("has response_headers", json.contains("\"response_headers\":[{\"content-type\":\"application/json\"}]"));
        } finally {
            logger.tracingStop().enable();
        }
    }

    @Test
    public void logsJsonPostWithHeadersTest() throws IOException, ServletException {
        HttpLogger logger = HttpLoggerFactory.get().disable().tracingStart();
        try {
            HttpLoggerForServlets filter = new HttpLoggerForServlets();
            filter.init(null);
            filter.doFilter(mockRequestWithBody2(), mockResponse(), mockHtmlApp());
            assertTrue("tracing history is 2", logger.tracingHistory().size() == 2);
            String json = logger.tracingHistory().get(0);
            assertTrue("json is valid", parseable(json));
            assertTrue("has category", json.contains("\"category\":\"http_request\""));
            assertTrue("has request_body", json.contains("\"request_body\":\"" + MOCK_JSON_ESCAPED + "\""));
            assertTrue("has request_headers",
                    json.contains("\"request_headers\":[{\"a\":\"1\"},{\"a\":\"2\"},{\"content-type\":\"application/json\"}]"));
            assertTrue("has request_method", json.contains("\"request_method\":\"POST\""));
            assertTrue("has request_url", json.contains("\"request_url\":\"" + MOCK_URL + '?' + MOCK_QUERY_STRING + "\""));
            json = logger.tracingHistory().get(1);
            assertTrue("json is valid", parseable(json));
            assertTrue("has category", json.contains("\"category\":\"http_response\""));
            assertTrue("has response_body", json.contains("\"response_body\":\"" + MOCK_HTML_ESCAPED + "\""));
            assertTrue("has response_code", json.contains("\"response_code\":\"404\""));
            assertTrue("has response_headers",
                    json.contains("\"response_headers\":[{\"a\":\"Z\"},{\"content-type\":\"text/html\"}]"));
        } finally {
            logger.tracingStop().enable();
        }
    }

    @Test
    public void skipsLoggingTest() throws IOException, ServletException {
        HttpLogger logger = HttpLoggerFactory.get().disable().tracingStart();
        try {
            HttpLoggerForServlets filter = new HttpLoggerForServlets();
            filter.init(null);
            filter.doFilter(mockRequest(), mockResponse(), mockCustomApp());
            filter.doFilter(mockRequest(), mockResponse(), mockCustomRedirectApp());
            filter.doFilter(mockRequest(), mockResponse(), mockHtmlRedirectApp());
            filter.doFilter(mockRequest(), mockResponse(), mockJsonRedirectApp());
            assertTrue("tracing history is 0", logger.tracingHistory().size() == 0);
        } finally {
            logger.tracingStop().enable();
        }
    }

}
