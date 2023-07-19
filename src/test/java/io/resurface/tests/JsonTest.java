// © 2016-2023 Graylog, Inc.

package io.resurface.tests;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static io.resurface.Json.*;

/**
 * Tests against utility methods for formatting JSON messages.
 */
public class JsonTest {

    @Test
    public void appendTest() {
        expect(append(new StringBuilder(), null, null).toString()).toEqual("");
        expect(append(new StringBuilder("XYZ"), null, null).toString()).toEqual("XYZ");
        expect(append(new StringBuilder(), "A", "").toString()).toEqual("\"A\",\"\"");
        expect(append(new StringBuilder(), "B", " ").toString()).toEqual("\"B\",\" \"");
        expect(append(new StringBuilder(), "C", "   ").toString()).toEqual("\"C\",\"   \"");
        expect(append(new StringBuilder(), "D", "\t").toString()).toEqual("\"D\",\"\\t\"");
        expect(append(new StringBuilder(), "E", "\t\t ").toString()).toEqual("\"E\",\"\\t\\t \"");
        expect(append(new StringBuilder("!"), "name-2", "value1").toString()).toEqual("!\"name-2\",\"value1\"");
        expect(append(new StringBuilder(), "s", "the cow says \"moo").toString()).toEqual("\"s\",\"the cow says \\\"moo\"");
        expect(append(new StringBuilder(), "1", "the cow says \"moo\"").toString()).toEqual("\"1\",\"the cow says \\\"moo\\\"\"");
    }

    @Test
    public void escapeBackslashTest() {
        expect(escape(new StringBuilder(), "\\the cow says moo").toString()).toEqual("\\\\the cow says moo");
        expect(escape(new StringBuilder(), "the cow says moo\\").toString()).toEqual("the cow says moo\\\\");
        expect(escape(new StringBuilder("{"), "the cow \\says moo").toString()).toEqual("{the cow \\\\says moo");
    }

    @Test
    public void escapeBackspaceTest() {
        expect(escape(new StringBuilder("{"), "\bthe cow says moo").toString()).toEqual("{\\bthe cow says moo");
        expect(escape(new StringBuilder(), "the cow\b says moo").toString()).toEqual("the cow\\b says moo");
        expect(escape(new StringBuilder(), "the cow says moo\b").toString()).toEqual("the cow says moo\\b");
    }

    @Test
    public void escapeFormFeedTest() {
        expect(escape(new StringBuilder(), "\fthe cow says moo").toString()).toEqual("\\fthe cow says moo");
        expect(escape(new StringBuilder(), "the cow\f says moo").toString()).toEqual("the cow\\f says moo");
        expect(escape(new StringBuilder(), "the cow says moo\f").toString()).toEqual("the cow says moo\\f");
    }

    @Test
    public void escapeNewLineTest() {
        expect(escape(new StringBuilder(), "\nthe cow says moo").toString()).toEqual("\\nthe cow says moo");
        expect(escape(new StringBuilder(), "the cow\n says moo").toString()).toEqual("the cow\\n says moo");
        expect(escape(new StringBuilder(), "the cow says moo\n").toString()).toEqual("the cow says moo\\n");
    }

    @Test
    public void escapeNullsTest() {
        expect(escape(new StringBuilder(), null).toString()).toEqual("");
        expect(escape(new StringBuilder("ABC"), null).toString()).toEqual("ABC");
    }

    @Test
    public void escapeQuoteTest() {
        expect(escape(new StringBuilder(), "\"the cow says moo").toString()).toEqual("\\\"the cow says moo");
        expect(escape(new StringBuilder(), "the cow says moo\"").toString()).toEqual("the cow says moo\\\"");
        expect(escape(new StringBuilder(), "the cow says \"moo").toString()).toEqual("the cow says \\\"moo");
    }

    @Test
    public void escapeReturnTest() {
        expect(escape(new StringBuilder(), "\rthe cow says moo").toString()).toEqual("\\rthe cow says moo");
        expect(escape(new StringBuilder(), "the cow\r says moo").toString()).toEqual("the cow\\r says moo");
        expect(escape(new StringBuilder(), "the cow says moo\r").toString()).toEqual("the cow says moo\\r");
    }

    @Test
    public void escapeTabTest() {
        expect(escape(new StringBuilder(), "\tthe cow says moo").toString()).toEqual("\\tthe cow says moo");
        expect(escape(new StringBuilder(), "the cow\t says moo").toString()).toEqual("the cow\\t says moo");
        expect(escape(new StringBuilder(), "the cow says moo\t").toString()).toEqual("the cow says moo\\t");
    }

    @Test
    public void escapeUnicodeTest() {
        expect(escape(new StringBuilder(), "\u001B").toString()).toEqual("\\u001B");
        expect(escape(new StringBuilder(" "), "\u007F").toString()).toEqual(" \\u007F");
        expect(escape(new StringBuilder(), "\u204B").toString()).toEqual("\\u204B");
        expect(escape(new StringBuilder(), "\u005B").toString()).toEqual("[");
        expect(escape(new StringBuilder(), " ö").toString()).toEqual(" ö");
        expect(escape(new StringBuilder(), "  \u00F6 has \ndiaeresis").toString()).toEqual("  ö has \\ndiaeresis");
        expect(escape(new StringBuilder(), "\u9BE8").toString()).toEqual("鯨");
        expect(escape(new StringBuilder(), "鯨 is a whale").toString()).toEqual("鯨 is a whale");
    }

    @Test
    public void stringifyTest() {
        List<String[]> message = new ArrayList<>();
        message.add(new String[]{"A", "B"});
        expect(stringify(message)).toEqual("[[\"A\",\"B\"]]");
        message.add(new String[]{"C1", "D2"});
        expect(stringify(message)).toEqual("[[\"A\",\"B\"],[\"C1\",\"D2\"]]");
    }

}
