// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface.tests;

import com.google.gson.Gson;
import io.resurface.HttpServletRequestImpl;
import io.resurface.HttpServletResponseImpl;
import io.resurface.JsonMessage;
import io.resurface.UsageLoggers;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

/**
 * Provides mock objects and utilities for testing.
 */
public class Helper {

    static final String MOCK_JSON = "{ \"hello\" : \"world\" }";

    static final String MOCK_JSON_ESCAPED = JsonMessage.escape(new StringBuilder(), MOCK_JSON).toString();

    static final String MOCK_HTML = "<html>Hello World!</html>";

    static final String MOCK_HTML_ESCAPED = JsonMessage.escape(new StringBuilder(), MOCK_HTML).toString();

    static final long MOCK_NOW = 1455908640173L;

    static final String MOCK_QUERY_STRING = "foo=bar";

    static final String MOCK_URL = "http://something.com/index.html";

    static final String[] URLS_DENIED = {UsageLoggers.urlForDemo() + "/noway3is5this1valid2",
            "https://www.noway3is5this1valid2.com/"};

    static final String[] URLS_INVALID = {"", "noway3is5this1valid2", "http://www.noway3is5this1valid2.com/"};

    static FilterChain mockCustomApp() {
        return (req, res) -> {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setContentType("application/super-troopers");
            response.setStatus(999);
        };
    }

    static FilterChain mockCustomRedirectApp() {
        return (req, res) -> {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/whatever");
            response.setStatus(304);
        };
    }

    static FilterChain mockExceptionApp() {
        return (req, res) -> {
            throw new UnsupportedEncodingException("simulated failure");
        };
    }

    static FilterChain mockJsonApp() {
        return (req, res) -> {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getOutputStream().write(MOCK_JSON.getBytes());
            response.setStatus(500);
        };
    }

    static FilterChain mockJsonRedirectApp() {
        return (req, res) -> {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.setStatus(304);
        };
    }

    static FilterChain mockHtmlApp() {
        return (req, res) -> {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html");
            response.setHeader("A", "Z");
            response.getOutputStream().write(MOCK_HTML.getBytes());
            response.setStatus(404);
        };
    }

    static FilterChain mockHtmlRedirectApp() {
        return (req, res) -> {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=utf-8");
            response.setStatus(304);
        };
    }

    static HttpServletRequestImpl mockRequest() {
        HttpServletRequestImpl r = new HttpServletRequestImpl();
        r.setMethod("GET");
        r.setRequestURL(MOCK_URL);
        return r;
    }

    static HttpServletRequestImpl mockRequestWithBody() throws UnsupportedEncodingException {
        HttpServletRequestImpl r = new HttpServletRequestImpl(MOCK_JSON.getBytes());
        r.setCharacterEncoding("UTF-8");
        r.setContentType("application/json");
        r.setMethod("POST");
        r.setQueryString(MOCK_QUERY_STRING);
        r.setRequestURL(MOCK_URL);
        return r;
    }

    static HttpServletRequestImpl mockRequestWithBody2() throws UnsupportedEncodingException {
        HttpServletRequestImpl impl = mockRequestWithBody();
        impl.addHeader("A", "1");
        impl.addHeader("A", "2");
        return impl;
    }

    static HttpServletResponseImpl mockResponse() {
        HttpServletResponseImpl r = new HttpServletResponseImpl();
        r.setCharacterEncoding("UTF-8");
        r.setStatus(200);
        return r;
    }

    static boolean parseable(String json) {
        if (json == null || !json.startsWith("{") || !json.endsWith("}")) return false;
        try {
            parser.fromJson(json, Object.class);
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    private static final Gson parser = new Gson();

}
