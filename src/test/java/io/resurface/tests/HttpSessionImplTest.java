// © 2016-2021 Resurface Labs Inc.

package io.resurface.tests;

import io.resurface.HttpSessionImpl;
import org.junit.Test;

import javax.servlet.http.HttpSession;
import java.util.Enumeration;

import static com.mscharhag.oleaster.matcher.Matchers.expect;

/**
 * Tests against mock HttpSession implementation.
 */
public class HttpSessionImplTest {

    @Test
    public void useAttributesTest() {
        HttpSession session = new HttpSessionImpl();
        session.setAttribute("A", "1");
        expect(session.getAttribute("A")).toEqual("1");
        session.setAttribute("B", "2");
        expect(session.getAttribute("A")).toEqual("1");
        expect(session.getAttribute("B")).toEqual("2");

        Enumeration<String> e = session.getAttributeNames();
        int attrs = 0;
        while (e.hasMoreElements()) {
            e.nextElement();
            attrs++;
        }
        expect(attrs).toEqual(2);
    }

}
