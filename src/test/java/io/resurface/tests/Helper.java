// © 2016-2023 Graylog, Inc.

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

    static final String MOCK_HTML = "<html>Hello World!</html>";

    static final String MOCK_HTML2 = "<html>Hola Mundo!</html>";

    static final String MOCK_HTML3 = "<html>1 World 2 World Red World Blue World!</html>";

    static final String MOCK_HTML4 = "<html>1 World\n2 World\nRed World \nBlue World!\n</html>";

    static final String MOCK_HTML5 = "<html>\n"
            + "<input type=\"hidden\">SENSITIVE</input>\n"
            + "<input class='foo' type=\"hidden\">\n"
            + "SENSITIVE\n"
            + "</input>\n"
            + "</html>";

    static final String MOCK_JSON = "{ \"hello\" : \"world\" }";

    static final String MOCK_JSON_ESCAPED = Json.escape(new StringBuilder(), MOCK_JSON).toString();

    static final long MOCK_NOW = 1455908640173L;

    static final String MOCK_QUERY_STRING = "foo=bar";

    static final String MOCK_URL = "http://something.com:3000/index.html";

    static final String[] MOCK_URLS_DENIED = {Helper.DEMO_URL + "/noway3is5this1valid2",
            "https://www.noway3is5this1valid2.com/"};

    static final String[] MOCK_URLS_INVALID = {"", "noway3is5this1valid2", "ftp:\\www.noway3is5this1valid2.com/",
            "urn:ISSN:1535–3613"};

    static final String MOCK_MESSAGE = "[[\"message\"], [123]]";

    static FilterChain mockCustomApp() {
        return (req, res) -> {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setContentType("application/super-troopers");
            response.setStatus(200);
        };
    }

    static FilterChain mockCustom404App() {
        return (req, res) -> {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/whatever");
            response.setStatus(404);
        };
    }

    static FilterChain mockExceptionApp() {
        return (req, res) -> {
            throw new UnsupportedEncodingException("simulated failure");
        };
    }

    static FilterChain mockHtmlApp() {
        return (req, res) -> {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html");
            response.setHeader("A", "Z");
            response.getOutputStream().write(MOCK_HTML.getBytes());
            response.setStatus(200);
        };
    }

    static FilterChain mockHtml404App() {
        return (req, res) -> {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=utf-8");
            response.setStatus(404);
        };
    }

    static FilterChain mockJsonApp() {
        return (req, res) -> {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            response.getOutputStream().write(MOCK_JSON.getBytes());
            response.setStatus(200);
        };
    }

    static FilterChain mockJson404App() {
        return (req, res) -> {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.setStatus(404);
        };
    }

    static HttpServletRequestImpl mockRequest() {
        HttpServletRequestImpl r = new HttpServletRequestImpl();
        r.setMethod("GET");
        r.setRequestURL(MOCK_URL);
        return r;
    }

    static HttpServletRequestImpl mockRequestWithJson() {
        HttpServletRequestImpl r = new HttpServletRequestImpl();
        r.addHeader("Content-Type", "Application/JSON");
        r.addParam("message", MOCK_JSON);
        r.setMethod("POST");
        r.setQueryString(MOCK_QUERY_STRING);
        r.setRequestURL(MOCK_URL);
        return r;
    }

    static HttpServletRequestImpl mockRequestWithJson2() {
        HttpServletRequestImpl r = mockRequestWithJson();
        r.addHeader("ABC", "123");
        r.addHeader("A", "1");
        r.addHeader("A", "2");
        r.addParam("ABC", "123");
        r.addParam("ABC", "234");
        return r;
    }

    static HttpServletResponseImpl mockResponse() {
        HttpServletResponseImpl r = new HttpServletResponseImpl();
        r.setCharacterEncoding("UTF-8");
        r.setStatus(200);
        return r;
    }

    static HttpServletResponseImpl mockResponseWithHtml() {
        HttpServletResponseImpl r = mockResponse();
        r.setContentType("text/html; charset=utf-8");
        return r;
    }

    static boolean parseable(String msg) {
        if (msg == null || !msg.trim().startsWith("[") || !msg.trim().endsWith("]")
                || msg.contains("[]") || (msg.contains(",,"))) return false;
        try {
            parser.fromJson(msg, Object.class);
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    private static final Gson parser = new Gson();

}
