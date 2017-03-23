// © 2016-2017 Resurface Labs LLC

package io.resurface.tests;

import org.junit.Test;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static io.resurface.JsonMessage.*;

/**
 * Tests against utility methods for formatting JSON messages.
 */
public class JsonMessageTest {

    @Test
    public void appendNullsTest() {
        expect(append(new StringBuilder(), null).toString()).toEqual("");
        expect(append(new StringBuilder("ABC"), null).toString()).toEqual("ABC");
        expect(append(new StringBuilder(), null, -1).toString()).toEqual("");
        expect(append(new StringBuilder("123"), null, -1).toString()).toEqual("123");
        expect(append(new StringBuilder(), null, null).toString()).toEqual("");
        expect(append(new StringBuilder("XYZ"), null, null).toString()).toEqual("XYZ");
    }

    @Test
    public void appendNumbersTest() {
        expect(append(new StringBuilder(), "name", -1).toString()).toEqual("\"name\":\"-1\"");
        expect(append(new StringBuilder(), "name1", 123).toString()).toEqual("\"name1\":\"123\"");
        expect(append(new StringBuilder("{"), "1_name1", 1455908665227L).toString()).toEqual("{\"1_name1\":\"1455908665227\"");
    }

    @Test
    public void appendStringsTest() {
        expect(append(new StringBuilder(), "A", "").toString()).toEqual("\"A\":\"\"");
        expect(append(new StringBuilder(), "B", " ").toString()).toEqual("\"B\":\" \"");
        expect(append(new StringBuilder(), "C", "   ").toString()).toEqual("\"C\":\"   \"");
        expect(append(new StringBuilder(), "D", "\t").toString()).toEqual("\"D\":\"\\t\"");
        expect(append(new StringBuilder(), "E", "\t\t ").toString()).toEqual("\"E\":\"\\t\\t \"");
        expect(append(new StringBuilder("!"), "name-2", "value1").toString()).toEqual("!\"name-2\":\"value1\"");
        expect(append(new StringBuilder(), "s", "the cow says \"moo").toString()).toEqual("\"s\":\"the cow says \\\"moo\"");
        expect(append(new StringBuilder(), "1", "the cow says \"moo\"").toString()).toEqual("\"1\":\"the cow says \\\"moo\\\"\"");
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
    public void startTest() {
        StringBuilder json = start(new StringBuilder(), "category1", "agent1", "version1", 1455908589662L);
        expect(json.toString()).toContain("{\"category\":\"category1\",");
        expect(json.toString()).toContain("\"agent\":\"agent1\",");
        expect(json.toString()).toContain("\"version\":\"version1\",");
        expect(json.toString()).toContain("\"now\":\"1455908589662\"");
    }

    @Test
    public void stopTest() {
        expect(stop(new StringBuilder()).toString()).toEqual("}");
    }

}
