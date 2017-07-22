// © 2016-2017 Resurface Labs LLC

package io.resurface.tests;

import com.google.gson.Gson;
import io.resurface.HttpServletRequestImpl;
import io.resurface.HttpServletResponseImpl;
import io.resurface.Json;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

/**
 * Provides mock objects and utilities for testing.
 */
public class Helper {

    static final String DEMO_URL = "https://demo.resurface.io/ping";

    static final String MOCK_AGENT = "helper.java";

    static final String MOCK_FORM_CHECKBOX = "a=A1&a=A2&a=A3";

    static final String MOCK_FORM_REGISTER = "firstname=wreck+it&lastname=ralph&middle=&=";

    static final String MOCK_JSON = "{ \"hello\" : \"world\" }";

    static final String MOCK_JSON_ESCAPED = Json.escape(new StringBuilder(), MOCK_JSON).toString();

    static final String MOCK_HTML = "<html>Hello World!</html>";

    static final long MOCK_NOW = 1455908640173L;

    static final String MOCK_QUERY_STRING = "foo=bar";

    static final String MOCK_URL = "http://something.com:3000/index.html";

    static final String[] MOCK_URLS_DENIED = {Helper.DEMO_URL + "/noway3is5this1valid2",
            "https://www.noway3is5this1valid2.com/"};

    static final String[] MOCK_URLS_INVALID = {"", "noway3is5this1valid2", "ftp:\\www.noway3is5this1valid2.com/",
            "urn:ISSN:1535–3613"};

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
        r.setContentType("Application/JSON");
        r.setMethod("POST");
        r.setQueryString(MOCK_QUERY_STRING);
        r.setRequestURL(MOCK_URL);
        return r;
    }

    static HttpServletRequestImpl mockRequestWithBody2() throws UnsupportedEncodingException {
        HttpServletRequestImpl r = mockRequestWithBody();
        r.addHeader("ABC", "123");
        r.addHeader("A", "1");
        r.addHeader("A", "2");
        return r;
    }

    static HttpServletRequestImpl mockRequestWithFormCheckbox() throws UnsupportedEncodingException {
        HttpServletRequestImpl r = new HttpServletRequestImpl(MOCK_FORM_CHECKBOX.getBytes());
        r.setCharacterEncoding("UTF-8");
        r.setContentType("application/x-www-form-urlencoded");
        r.setMethod("POST");
        r.setQueryString(MOCK_QUERY_STRING);
        r.setRequestURL(MOCK_URL);
        return r;
    }

    static HttpServletRequestImpl mockRequestWithFormRegister() throws UnsupportedEncodingException {
        HttpServletRequestImpl r = new HttpServletRequestImpl(MOCK_FORM_REGISTER.getBytes());
        r.setCharacterEncoding("UTF-8");
        r.setContentType("application/X-WWW-FORM-URLENCODED");
        r.setMethod("POST");
        r.setQueryString(MOCK_QUERY_STRING);
        r.setRequestURL(MOCK_URL);
        return r;
    }

    static HttpServletResponseImpl mockResponse() {
        HttpServletResponseImpl r = new HttpServletResponseImpl();
        r.setCharacterEncoding("UTF-8");
        r.setStatus(200);
        return r;
    }

    static HttpServletResponseImpl mockResponseWithBody() {
        HttpServletResponseImpl r = new HttpServletResponseImpl();
        r.setCharacterEncoding("UTF-8");
        r.setContentType("text/html; charset=utf-8");
        r.setStatus(200);
        return r;
    }

    static boolean parseable(String json) {
        if (json == null || !json.startsWith("[") || !json.endsWith("]")) return false;
        try {
            parser.fromJson(json, Object.class);
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    private static final Gson parser = new Gson();

}
