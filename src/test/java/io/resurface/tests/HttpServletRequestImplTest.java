// © 2016-2021 Resurface Labs Inc.

package io.resurface.tests;

import io.resurface.HttpServletRequestImpl;
import org.junit.Test;

import javax.servlet.http.HttpSession;
import java.util.Enumeration;

import static com.mscharhag.oleaster.matcher.Matchers.expect;

/**
 * Tests against mock HttpServletRequest implementation.
 */
public class HttpServletRequestImplTest {

    @Test
    public void useContentTypeTest() {
        HttpServletRequestImpl impl = new HttpServletRequestImpl();
        expect(impl.getHeader("CONTENT-TYPE")).toBeNull();

        String val = "text/html";
        impl.setContentType(val);
        expect(impl.getContentType()).toEqual(val);
        expect(impl.getHeader("Content-Type")).toEqual(val);
        expect(impl.getHeader("content-type")).toBeNull();

        impl.setContentType(null);
        expect(impl.getContentType()).toBeNull();
        expect(impl.getHeader("content-TYPE")).toBeNull();
    }

    @Test
    public void useHeadersTest() {
        String key = "2345";
        String key2 = "fish";
        String val = "u-turn";
        String val2 = "swell";

        HttpServletRequestImpl impl = new HttpServletRequestImpl();
        expect(impl.getHeader(key)).toBeNull();

        impl.setHeader(key, val);
        expect(impl.getHeaderNames().nextElement()).toEqual(key);
        expect(impl.getHeader(key)).toEqual(val);
        expect(impl.getHeaders(key).nextElement()).toEqual(val);

        impl.setHeader(key, val2);
        expect(impl.getHeaderNames().nextElement()).toEqual(key);
        expect(impl.getHeader(key)).toEqual(val2);
        expect(impl.getHeaders(key).nextElement()).toEqual(val2);

        impl.addHeader(key, val);
        expect(impl.getHeaderNames().nextElement()).toEqual(key);
        expect(impl.getHeader(key)).toEqual(val2);
        Enumeration e = impl.getHeaders(key);
        expect(e.nextElement()).toEqual(val2);
        expect(e.nextElement()).toEqual(val);

        impl.setHeader(key2, val2);
        e = impl.getHeaderNames();
        expect(e.nextElement()).toEqual(key);
        expect(e.nextElement()).toEqual(key2);
        expect(impl.getHeader(key2)).toEqual(val2);
        expect(impl.getHeader(key2.toUpperCase())).toBeNull();
    }

    @Test
    public void useMethodTest() {
        String val = "!METHOD!";
        HttpServletRequestImpl impl = new HttpServletRequestImpl();
        expect(impl.getMethod()).toBeNull();
        impl.setMethod(val);
        expect(impl.getMethod()).toEqual(val);
    }

    @Test
    public void useParamsTest() {
        String key = "2345";
        String key2 = "fish";
        String val = "u-turn";
        String val2 = "swell";

        HttpServletRequestImpl impl = new HttpServletRequestImpl();
        expect(impl.getParameter(key)).toBeNull();
        expect(impl.getParameterValues(key)).toBeNull();

        impl.addParam(key, val);
        expect(impl.getParameterNames().nextElement()).toEqual(key);
        expect(impl.getParameter(key)).toEqual(val);
        expect(impl.getParameterValues(key)[0]).toEqual(val);

        impl.addParam(key, val2);
        expect(impl.getParameterNames().nextElement()).toEqual(key);
        expect(impl.getParameter(key)).toEqual(val);
        expect(impl.getParameterValues(key)[0]).toEqual(val);
        expect(impl.getParameterValues(key)[1]).toEqual(val2);

        impl.addParam(key2, val2);
        Enumeration e = impl.getParameterNames();
        expect(e.nextElement()).toEqual(key);
        expect(e.nextElement()).toEqual(key2);
        expect(impl.getParameter(key2)).toEqual(val2);
        expect(impl.getParameter(key2.toUpperCase())).toBeNull();
    }

    @Test
    public void useRequestURLTest() {
        String val = "http://resurface.io/yadda";
        HttpServletRequestImpl impl = new HttpServletRequestImpl();
        expect(impl.getRequestURL()).toBeNull();
        impl.setRequestURL(val);
        expect(impl.getRequestURL().toString()).toEqual(val);
    }

    @Test
    public void useSessionTest() {
        HttpServletRequestImpl impl = new HttpServletRequestImpl();
        HttpSession session = impl.getSession(false);
        expect(session).toBeNull();

        session = impl.getSession();
        expect(session).toBeNotNull();
        session.setAttribute("A", "1");
        expect(session.getAttribute("A")).toEqual("1");

        session = impl.getSession(false);
        expect(session.getAttribute("A")).toEqual("1");

        session = impl.getSession(true);
        expect(session.getAttribute("A")).toEqual("1");
    }

}
