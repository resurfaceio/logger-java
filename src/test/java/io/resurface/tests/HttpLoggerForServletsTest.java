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
            assertTrue("has body", !json.contains("\"body\""));
            assertTrue("has category", json.contains("\"category\":\"http_request\""));
            assertTrue("has headers", json.contains("\"headers\":[]"));
            assertTrue("has method", json.contains("\"method\":\"GET\""));
            assertTrue("has url", json.contains("\"url\":\"" + MOCK_URL + "\""));
            json = logger.tracingHistory().get(1);
            assertTrue("json is valid", parseable(json));
            assertTrue("has body", json.contains("\"body\":\"" + MOCK_HTML_ESCAPED + "\""));
            assertTrue("has category", json.contains("\"category\":\"http_response\""));
            assertTrue("has code", json.contains("\"code\":404"));
            assertTrue("has headers", json.contains("\"headers\":[]"));
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
            assertTrue("has body", !json.contains("\"body\""));
            assertTrue("has category", json.contains("\"category\":\"http_request\""));
            assertTrue("has headers", json.contains("\"headers\":[]"));
            assertTrue("has method", json.contains("\"method\":\"GET\""));
            assertTrue("has url", json.contains("\"url\":\"" + MOCK_URL + "\""));
            json = logger.tracingHistory().get(1);
            assertTrue("json is valid", parseable(json));
            assertTrue("has body", json.contains("\"body\":\"" + MOCK_JSON_ESCAPED + "\""));
            assertTrue("has category", json.contains("\"category\":\"http_response\""));
            assertTrue("has code", json.contains("\"code\":500"));
            assertTrue("has headers", json.contains("\"headers\":[]"));
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
            assertTrue("has body", json.contains("\"body\":\"" + MOCK_JSON_ESCAPED + "\""));
            assertTrue("has category", json.contains("\"category\":\"http_request\""));
            assertTrue("has headers", json.contains("\"headers\":[{\"Content-Type\":\"application/json\"}]"));
            assertTrue("has method", json.contains("\"method\":\"POST\""));
            assertTrue("has url", json.contains("\"url\":\"" + MOCK_URL + "\""));
            json = logger.tracingHistory().get(1);
            assertTrue("json is valid", parseable(json));
            assertTrue("has body", json.contains("\"body\":\"" + MOCK_JSON_ESCAPED + "\""));
            assertTrue("has category", json.contains("\"category\":\"http_response\""));
            assertTrue("has code", json.contains("\"code\":500"));
            assertTrue("has headers", json.contains("\"headers\":[]"));
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
            filter.doFilter(mockRequestWithBodyAndHeaders(), mockResponse(), mockHtmlApp());
            assertTrue("tracing history is 2", logger.tracingHistory().size() == 2);
            String json = logger.tracingHistory().get(0);
            assertTrue("json is valid", parseable(json));
            assertTrue("has body", json.contains("\"body\":\"" + MOCK_JSON_ESCAPED + "\""));
            assertTrue("has category", json.contains("\"category\":\"http_request\""));
            assertTrue("has headers", json.contains("\"headers\":[{\"ABC\":\"123\"},{\"Content-Type\":\"application/json\"}]"));
            assertTrue("has method", json.contains("\"method\":\"POST\""));
            assertTrue("has url", json.contains("\"url\":\"" + MOCK_URL + "\""));
            json = logger.tracingHistory().get(1);
            assertTrue("json is valid", parseable(json));
            assertTrue("has body", json.contains("\"body\":\"" + MOCK_HTML_ESCAPED + "\""));
            assertTrue("has category", json.contains("\"category\":\"http_response\""));
            assertTrue("has code", json.contains("\"code\":404"));
            assertTrue("has headers", json.contains("\"headers\":[]"));
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
