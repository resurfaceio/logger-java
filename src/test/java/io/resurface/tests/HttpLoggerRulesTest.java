// © 2016-2018 Resurface Labs LLC

package io.resurface.tests;

import io.resurface.HttpLogger;
import io.resurface.HttpRule;
import io.resurface.HttpRules;
import org.junit.Test;

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
    public void managesDefaultRulesTest() {
        expect(HttpLogger.getDefaultRules()).toEqual(HttpRules.getStrictRules());
        try {
            HttpLogger.setDefaultRules("");
            expect(HttpLogger.getDefaultRules()).toEqual("");
            expect(HttpRules.parse(HttpLogger.getDefaultRules()).size()).toEqual(0);

            HttpLogger.setDefaultRules(" include default");
            expect(HttpLogger.getDefaultRules()).toEqual("");

            HttpLogger.setDefaultRules("include default\n");
            expect(HttpLogger.getDefaultRules()).toEqual("");

            HttpLogger.setDefaultRules("include default\ninclude default\n");
            expect(HttpRules.parse(HttpLogger.getDefaultRules()).size()).toEqual(0);

            HttpLogger.setDefaultRules("include default\ninclude default\nsample 42");
            List<HttpRule> rules = HttpRules.parse(HttpLogger.getDefaultRules());
            expect(rules.size()).toEqual(1);
            expect(rules.stream().filter(r -> "sample".equals(r.verb)).count()).toEqual(1);
        } finally {
            HttpLogger.setDefaultRules(HttpRules.getStrictRules());
        }
    }

    @Test
    public void overridesDefaultRulesTest() {
        expect(HttpLogger.getDefaultRules()).toEqual(HttpRules.getStrictRules());
        try {
            HttpLogger logger = new HttpLogger("https://mysite.com");
            expect(logger.getRules()).toEqual(HttpRules.getStrictRules());
            logger = new HttpLogger("https://mysite.com", "# 123");
            expect(logger.getRules()).toEqual("# 123");

            HttpLogger.setDefaultRules("");
            logger = new HttpLogger("https://mysite.com");
            expect(logger.getRules()).toEqual("");
            logger = new HttpLogger("https://mysite.com", "   ");
            expect(logger.getRules()).toEqual("");
            logger = new HttpLogger("https://mysite.com", " sample 42");
            expect(logger.getRules()).toEqual(" sample 42");

            HttpLogger.setDefaultRules("skip_compression");
            logger = new HttpLogger("https://mysite.com");
            expect(logger.getRules()).toEqual("skip_compression");
            logger = new HttpLogger("https://mysite.com", "include default\nskip_submission\n");
            expect(logger.getRules()).toEqual("skip_compression\nskip_submission\n");

            HttpLogger.setDefaultRules("sample 42\n");
            logger = new HttpLogger("https://mysite.com");
            expect(logger.getRules()).toEqual("sample 42\n");
            logger = new HttpLogger("https://mysite.com", "   ");
            expect(logger.getRules()).toEqual("sample 42\n");
            logger = new HttpLogger("https://mysite.com", "include default\nskip_submission\n");
            expect(logger.getRules()).toEqual("sample 42\n\nskip_submission\n");
        } finally {
            HttpLogger.setDefaultRules(HttpRules.getStrictRules());
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
        logger.log(request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"session_field:butterfly\",\"poison\"]")).toBeTrue();
        expect(queue.get(0).contains("[\"session_field:session_id\",\"asdf1234\"]")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "copy_session_field /session_id/");
        logger.log(request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"session_field:butterfly\",")).toBeFalse();
        expect(queue.get(0).contains("[\"session_field:session_id\",\"asdf1234\"]")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "copy_session_field /blah/");
        logger.log(request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"session_field:")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "copy_session_field /butterfly/\ncopy_session_field /session_id/");
        logger.log(request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
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
        logger.log(request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"session_field:")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "copy_session_field !.*!\n!session_field:butterfly! remove");
        logger.log(request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"session_field:butterfly\",")).toBeFalse();
        expect(queue.get(0).contains("[\"session_field:session_id\",\"asdf1234\"]")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "copy_session_field !.*!\n!session_field:.*! remove_if !poi.*!");
        logger.log(request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"session_field:butterfly\",")).toBeFalse();
        expect(queue.get(0).contains("[\"session_field:session_id\",\"asdf1234\"]")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "copy_session_field !.*!\n!session_field:.*! remove_unless !sugar!");
        logger.log(request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
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
        logger.log(request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "copy_session_field !.*!\n!session_field:butterfly! stop_if !poi.*!");
        logger.log(request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "copy_session_field !.*!\n!session_field:butterfly! stop_unless !sugar!");
        logger.log(request, mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(0);
    }

    @Test
    public void usesRemoveRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!.*! remove");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! remove");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! remove");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body|response_body! remove");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_header:.*! remove");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"request_header:")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_header:abc! remove\n!response_body! remove");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
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
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!.*! remove_if !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! remove_if !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! remove_if !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_if !.*World.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_if !.*blahblahblah.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! remove_if !.*!\n!response_body! remove_if !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();
    }

    @Test
    public void usesRemoveIfFoundRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_header:blahblahblah! remove_if_found !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!.*! remove_if_found !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! remove_if_found !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! remove_if_found !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_if_found !World!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_if_found !.*World.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_if_found !blahblahblah!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();
    }

    @Test
    public void usesRemoveUnlessRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_header:blahblahblah! remove_unless !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!.*! remove_unless !.*blahblahblah.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! remove_unless !.*blahblahblah.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! remove_unless !.*blahblahblah.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_unless !.*World.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_unless !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! remove_unless !.*!\n!request_body! remove_unless !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();
    }

    @Test
    public void usesRemoveUnlessFoundRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_header:blahblahblah! remove_unless_found !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!.*! remove_unless_found !blahblahblah!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! remove_unless_found !blahblahblah!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! remove_unless_found !blahblahblah!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_unless_found !World!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_unless_found !.*World.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeFalse();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body|request_body! remove_unless_found !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"request_body\",")).toBeTrue();
        expect(queue.get(0).contains("[\"response_body\",")).toBeTrue();
    }

    @Test
    public void usesReplaceRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_body! replace !blahblahblah!, !ZZZZZ!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("World")).toBeTrue();
        expect(queue.get(0).contains("ZZZZZ")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! replace !World!, !Mundo!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>Hello Mundo!</html>\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body|response_body! replace !^.*!, !ZZZZZ!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"request_body\",\"ZZZZZ\"],");
        expect(queue.get(0)).toContain("[\"response_body\",\"ZZZZZ\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! replace !^.*!, !QQ!\n!response_body! replace !^.*!, !SS!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"request_body\",\"QQ\"],");
        expect(queue.get(0)).toContain("[\"response_body\",\"SS\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! replace !World!, !!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>Hello !</html>\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! replace !.*!, !!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0).contains("[\"response_body\",")).toBeFalse();

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! replace !World!, !Z!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML3, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>1 Z 2 Z Red Z Blue Z!</html>\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! replace !World!, !Z!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML4, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>1 Z\\n2 Z\\nRed Z \\nBlue Z!\\n</html>\"],");
    }

    @Test
    public void usesReplaceRulesWithComplexExpressionsTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "/response_body/ replace /[a-zA-Z0-9.!#$%&’*+\\/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)/, /x@y.com/");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML.replace("World", "rob@resurface.io"), MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>Hello x@y.com!</html>\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "/response_body/ replace /[0-9\\.\\-\\/]{9,}/, /xyxy/");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML.replace("World", "123-45-1343"), MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>Hello xyxy!</html>\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! replace !World!, !<b>$0</b>!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>Hello <b>World</b>!</html>\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! replace !(World)!, !<b>$1</b>!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>Hello <b>World</b>!</html>\"],");

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! replace !<input([^>]*)>([^<]*)</input>!, !<input$1></input>!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML5, MOCK_JSON);
        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toContain("[\"response_body\",\"<html>\\n<input type=\\\"hidden\\\"></input>\\n<input class='foo' type=\\\"hidden\\\"></input>\\n</html>\"],");
    }

    @Test
    public void usesSampleRulesTest() {
        List<String> queue = new ArrayList<>();

        try {
            new HttpLogger(queue, "sample 10\nsample 99");
            expect(false).toBeTrue();
        } catch (IllegalArgumentException iae) {
            expect(iae.getMessage()).toEqual("Multiple sample rules");
        }

        HttpLogger logger = new HttpLogger(queue, "sample 10");
        for (int i = 1; i <= 100; i++) {
            logger.log(mockRequestWithJson2(), mockResponseWithHtml());
        }
        expect(queue.size()).toBeBetween(2, 20);
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
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!.*! stop");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! stop");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), null, MOCK_JSON);
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!request_body! stop\n!response_body! stop");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), null, MOCK_JSON);
        expect(queue.size()).toEqual(0);
    }

    @Test
    public void usesStopIfRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_header:blahblahblah! stop_if !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_if !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_if !.*World.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_if !.*blahblahblah.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
    }

    @Test
    public void usesStopIfFoundRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_header:blahblahblah! stop_if_found !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_if_found !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_if_found !World!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_if_found !.*World.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_if_found !blahblahblah!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, MOCK_JSON);
        expect(queue.size()).toEqual(1);
    }

    @Test
    public void usesStopUnlessRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_header:blahblahblah! stop_unless !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_unless !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_unless !.*World.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_unless !.*blahblahblah.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        expect(queue.size()).toEqual(0);
    }

    @Test
    public void usesStopUnlessFoundRulesTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "!response_header:blahblahblah! stop_unless_found !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        expect(queue.size()).toEqual(0);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_unless_found !.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_unless_found !World!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_unless_found !.*World.*!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        expect(queue.size()).toEqual(1);

        queue = new ArrayList<>();
        logger = new HttpLogger(queue, "!response_body! stop_unless_found !blahblahblah!");
        logger.log(mockRequestWithJson2(), mockResponseWithHtml(), MOCK_HTML, null);
        expect(queue.size()).toEqual(0);
    }

}
