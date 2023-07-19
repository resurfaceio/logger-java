// © 2016-2023 Graylog, Inc.

package io.resurface.tests;

import io.resurface.HttpLogger;
import io.resurface.HttpMessage;
import io.resurface.HttpRules;
import org.junit.Test;
import org.junit.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static io.resurface.tests.Helper.*;

/**
 * Tests against usage logger for HTTP/HTTPS protocol.
 */
public class HttpLoggerRulesTest {

    @Test
    public void overridesDefaultRulesTest() {
        expect(HttpRules.getDefaultRules()).toEqual(HttpRules.getStrictRules());
        try {
            HttpLogger logger = new HttpLogger("https://mysite.com");
            expect(logger.getRules().text).toEqual(HttpRules.getStrictRules());
            logger = new HttpLogger("https://mysite.com", "# 123");
            expect(logger.getRules().text).toEqual("# 123");

            HttpRules.setDefaultRules("");
            logger = new HttpLogger("https://mysite.com");
            expect(logger.getRules().text).toEqual("");
            logger = new HttpLogger("https://mysite.com", "   ");
            expect(logger.getRules().text).toEqual("");
            logger = new HttpLogger("https://mysite.com", " sample 42");
            expect(logger.getRules().text).toEqual(" sample 42");

            HttpRules.setDefaultRules("skip_compression");
            logger = new HttpLogger("https://mysite.com");
            expect(logger.getRules().text).toEqual("skip_compression");
            logger = new HttpLogger("https://mysite.com", "include default\nskip_submission\n");
            expect(logger.getRules().text).toEqual("skip_compression\nskip_submission\n");

            HttpRules.setDefaultRules("sample 42\n");
            logger = new HttpLogger("https://mysite.com");
            expect(logger.getRules().text).toEqual("sample 42\n");
            logger = new HttpLogger("https://mysite.com", "   ");
            expect(logger.getRules().text).toEqual("sample 42\n");
            logger = new HttpLogger("https://mysite.com", "include default\nskip_submission\n");
            expect(logger.getRules().text).toEqual("sample 42\n\nskip_submission\n");

            HttpRules.setDefaultRules("include debug");
            logger = new HttpLogger("https://mysite.com", HttpRules.STRICT_RULES);
            expect(logger.getRules().text).toEqual(HttpRules.STRICT_RULES);
        } finally {
            HttpRules.setDefaultRules(HttpRules.getStrictRules());
        }
    }

    @Test
    public void usesAllowHttpUrlRulesTest() {
        HttpLogger logger = new HttpLogger("http://mysite.com");
        expect(logger.isEnableable()).toBeFalse();
        logger = new HttpLogger("http://mysite.com", "");
        expect(logger.isEnableable()).toBeFalse();
        logger = new HttpLogger("https://mysite.com");
        expect(logger.isEnableable()).toBeTrue();
        logger = new HttpLogger("https://mysite.com", "allow_http_url");
        expect(logger.isEnableable()).toBeTrue();
        logger = new HttpLogger("https://mysite.com", "allow_http_url\nallow_http_url");
        expect(logger.isEnableable()).toBeTrue();
    }

    @Test
    public void usesCopySessionFieldRulesTest() {
        HttpServletRequest request = mockRequestWithJson2();
        request.getSession().setAttribute("butterfly", "poison");
        request.getSession().setAttribute("session_id", "asdf1234");

        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "copy_session_field /.*/");
        logger.init_dispatcher();
        HttpMessage.send(logger, request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"session_field:butterfly\",\"poison\"]")).toBeTrue();
        expect(queue.get(0).contains("[\"session_field:session_id\",\"asdf1234\"]")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "copy_session_field /session_id/");
        logger.init_dispatcher();
        HttpMessage.send(logger, request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"session_field:butterfly\",")).toBeFalse();
        expect(queue.get(0).contains("[\"session_field:session_id\",\"asdf1234\"]")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "copy_session_field /blah/");
        logger.init_dispatcher();
        HttpMessage.send(logger, request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"session_field:")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "copy_session_field /butterfly/\ncopy_session_field /session_id/");
        logger.init_dispatcher();
        HttpMessage.send(logger, request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"session_field:butterfly\",\"poison\"]")).toBeTrue();
        expect(queue.get(0).contains("[\"session_field:session_id\",\"asdf1234\"]")).toBeTrue();
    }

    @Test
    public void usesCopySessionFieldAndRemoveRulesTest() {
        HttpServletRequest request = mockRequestWithJson2();
        request.getSession().setAttribute("butterfly", "poison");
        request.getSession().setAttribute("session_id", "asdf1234");

        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "copy_session_field !.*!\n!session_field:.*! remove");
        logger.init_dispatcher();
        HttpMessage.send(logger, request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"session_field:")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "copy_session_field !.*!\n!session_field:butterfly! remove");
        logger.init_dispatcher();
        HttpMessage.send(logger, request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"session_field:butterfly\",")).toBeFalse();
        expect(queue.get(0).contains("[\"session_field:session_id\",\"asdf1234\"]")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "copy_session_field !.*!\n!session_field:.*! remove_if !poi.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"session_field:butterfly\",")).toBeFalse();
        expect(queue.get(0).contains("[\"session_field:session_id\",\"asdf1234\"]")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "copy_session_field !.*!\n!session_field:.*! remove_unless !sugar!");
        logger.init_dispatcher();
        HttpMessage.send(logger, request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"session_field:")).toBeFalse();
    }

    @Test
    public void usesCopySessionFieldAndStopRulesTest() {
        HttpServletRequest request = mockRequestWithJson2();
        request.getSession().setAttribute("butterfly", "poison");
        request.getSession().setAttribute("session_id", "asdf1234");

        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "copy_session_field !.*!\n!session_field:butterfly! stop");
        logger.init_dispatcher();
        HttpMessage.send(logger, request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "copy_session_field !.*!\n!session_field:butterfly! stop_if !poi.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "copy_session_field !.*!\n!session_field:butterfly! stop_unless !sugar!");
        logger.init_dispatcher();
        HttpMessage.send(logger, request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);
    }

    @Test
    public void usesRemoveRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!.*! remove");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! remove");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! remove");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body|response_body! remove");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_header:.*! remove");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"request_header:")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_header:abc! remove\n!response_body! remove");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"request_header:")).toBeTrue();
        expect(queue.get(0).contains("[\"request_header:abc\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();
    }

    @Test
    public void usesRemoveIfRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_header:blahblahblah! remove_if !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!.*! remove_if !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! remove_if !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! remove_if !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_if !.*World.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_if !.*blahblahblah.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! remove_if !.*!\n!response_body! remove_if !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();
    }

    @Test
    public void usesRemoveIfFoundRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_header:blahblahblah! remove_if_found !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!.*! remove_if_found !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! remove_if_found !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! remove_if_found !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_if_found !World!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_if_found !.*World.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_if_found !blahblahblah!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();
    }

    @Test
    public void usesRemoveUnlessRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_header:blahblahblah! remove_unless !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!.*! remove_unless !.*blahblahblah.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! remove_unless !.*blahblahblah.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! remove_unless !.*blahblahblah.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_unless !.*World.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_unless !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! remove_unless !.*!\n!request_body! remove_unless !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();
    }

    @Test
    public void usesRemoveUnlessFoundRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_header:blahblahblah! remove_unless_found !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!.*! remove_unless_found !blahblahblah!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! remove_unless_found !blahblahblah!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! remove_unless_found !blahblahblah!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_unless_found !World!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_unless_found !.*World.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_unless_found !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();
    }

    @Test
    public void usesReplaceRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_body! replace !blahblahblah!, !ZZZZZ!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("World")).toBeTrue();
        expect(queue.get(0).contains("ZZZZZ")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! replace !World!, !Mundo!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>Hello Mundo!</html>\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body|response_body! replace !^.*!, !ZZZZZ!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"request_body\",\"ZZZZZ\"],");
        expect(queue.get(0)).toContain("[\"response_body\",\"ZZZZZ\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! replace !^.*!, !QQ!\n!response_body! replace !^.*!, !SS!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"request_body\",\"QQ\"],");
        expect(queue.get(0)).toContain("[\"response_body\",\"SS\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! replace !World!, !!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>Hello !</html>\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! replace !.*!, !!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! replace !World!, !Z!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML3, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>1 Z 2 Z Red Z Blue Z!</html>\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! replace !World!, !Z!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML4, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>1 Z\\n2 Z\\nRed Z \\nBlue Z!\\n</html>\"],");
    }

    @Test
    public void usesReplaceRulesWithComplexExpressionsTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "/response_body/ replace /[a-zA-Z0-9.!#$%&’*+\\/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)/, /x@y.com/");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML.replace("World", "rob@resurface.io"), MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>Hello x@y.com!</html>\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "/response_body/ replace /[0-9\\.\\-\\/]{9,}/, /xyxy/");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML.replace("World", "123-45-1343"), MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>Hello xyxy!</html>\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! replace !World!, !<b>$0</b>!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>Hello <b>World</b>!</html>\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! replace !(World)!, !<b>$1</b>!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>Hello <b>World</b>!</html>\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! replace !<input([^>]*)>([^<]*)</input>!, !<input$1></input>!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML5, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>\\n<input type=\\\"hidden\\\"></input>\\n<input class='foo' type=\\\"hidden\\\"></input>\\n</html>\"],");
    }

    @Test
    public void usesSampleRulesTest() throws InterruptedException {
        List<String> queue = new ArrayList<>();
        List<String> flat_queue = new ArrayList<>();

        try {
            new HttpLogger(queue, "sample 10\nsample 99");
            expect(false).toBeTrue();
        } catch (IllegalArgumentException iae) {
            expect(iae.getMessage()).toEqual("Multiple sample rules");
        }

        HttpLogger logger = new HttpLogger(queue, "sample 10");
        logger.init_dispatcher();
        for (int i = 1; i <= 100; i++) {
            HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml());
        }
        Thread.sleep(10);
        logger.stop_dispatcher();
        for (String batch: queue) {
            for (String msg: batch.split("\n")) {
                flat_queue.add(msg);
            }
        }
        if (flat_queue.size() > 20 || flat_queue.size() < 2) System.out.println(flat_queue);
        expect(flat_queue.size()).toBeBetween(2, 20);
    }

    @Test
    public void usesSkipCompressionRulesTest() {
        HttpLogger logger = new HttpLogger("http://mysite.com");
        expect(logger.getSkipCompression()).toBeFalse();
        logger = new HttpLogger("http://mysite.com", "");
        expect(logger.getSkipCompression()).toBeFalse();
        logger = new HttpLogger("http://mysite.com", "skip_compression");
        expect(logger.getSkipCompression()).toBeTrue();
    }

    @Test
    public void usesSkipSubmissionRulesTest() {
        HttpLogger logger = new HttpLogger("http://mysite.com");
        expect(logger.getSkipSubmission()).toBeFalse();
        logger = new HttpLogger("http://mysite.com", "");
        expect(logger.getSkipSubmission()).toBeFalse();
        logger = new HttpLogger("http://mysite.com", "skip_submission");
        expect(logger.getSkipSubmission()).toBeTrue();
    }

    @Test
    public void usesStopRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_header:blahblahblah! stop");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!.*! stop");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! stop");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), null, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! stop\n!response_body! stop");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), null, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);
    }

    @Test
    public void usesStopIfRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_header:blahblahblah! stop_if !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_if !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_if !.*World.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_if !.*blahblahblah.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
    }

    @Test
    public void usesStopIfFoundRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_header:blahblahblah! stop_if_found !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_if_found !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_if_found !World!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_if_found !.*World.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_if_found !blahblahblah!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
    }

    @Test
    public void usesStopUnlessRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_header:blahblahblah! stop_unless !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_unless !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_unless !.*World.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_unless !.*blahblahblah.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);
    }

    @Test
    public void usesStopUnlessFoundRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_header:blahblahblah! stop_unless_found !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_unless_found !.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_unless_found !World!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_unless_found !.*World.*!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_unless_found !blahblahblah!");
        logger.init_dispatcher();
        HttpMessage.send(logger, mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(0);
    }

}
