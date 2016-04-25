// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides mock objects used for testing.
 */
public class Mocks {

    static final String MOCK_JSON = "{ \"hello\" : \"world\" }";

    static final String MOCK_JSON_ESCAPED = JsonMessage.escape(new StringBuilder(), MOCK_JSON).toString();

    static final String MOCK_HTML = "<html>Hello World!</html>";

    static final String MOCK_HTML_ESCAPED = JsonMessage.escape(new StringBuilder(), MOCK_HTML).toString();

    static final String[] MOCK_INVALID_URLS = {HttpLogger.DEFAULT_URL + "/noway3is5this1valid2",
            "https://www.noway3is5this1valid2.com/", "http://www.noway3is5this1valid2.com/"};

    static final String MOCK_URL = "http://something.com/index.html";

    static FilterChain mockCustomApp() {
        return (req, res) -> {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setCharacterEncoding("UTF-8");
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

    static FilterChain mockJsonApp() {
        return (req, res) -> {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
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

    static HttpServletRequest mockRequest() {
        HttpServletRequestImpl r = new HttpServletRequestImpl();
        r.setRequestURL(MOCK_URL);
        return r;
    }

    static HttpServletResponse mockResponse() {
        HttpServletResponseImpl r = new HttpServletResponseImpl();
        r.setStatus(200);
        return r;
    }

}
