// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface.tests;

import org.junit.Test;

import static io.resurface.JsonMessage.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests against utility methods for formatting JSON messages.
 */
public class JsonMessageTest {

    @Test
    public void appendNullsTest() {
        assertEquals("", append(new StringBuilder(), null).toString());
        assertEquals("ABC", append(new StringBuilder("ABC"), null).toString());
        assertEquals("", append(new StringBuilder(), null, -1).toString());
        assertEquals("123", append(new StringBuilder("123"), null, -1).toString());
        assertEquals("", append(new StringBuilder(), null, null).toString());
        assertEquals("XYZ", append(new StringBuilder("XYZ"), null, null).toString());
    }

    @Test
    public void appendNumbersTest() {
        assertEquals("\"name\":\"-1\"", append(new StringBuilder(), "name", -1).toString());
        assertEquals("\"name1\":\"123\"", append(new StringBuilder(), "name1", 123).toString());
        assertEquals("{\"1_name1\":\"1455908665227\"", append(new StringBuilder("{"), "1_name1", 1455908665227L).toString());
    }

    @Test
    public void appendStringsTest() {
        assertEquals("\"A\":\"\"", append(new StringBuilder(), "A", "").toString());
        assertEquals("\"B\":\" \"", append(new StringBuilder(), "B", " ").toString());
        assertEquals("\"C\":\"   \"", append(new StringBuilder(), "C", "   ").toString());
        assertEquals("\"D\":\"\\t\"", append(new StringBuilder(), "D", "\t").toString());
        assertEquals("\"E\":\"\\t\\t \"", append(new StringBuilder(), "E", "\t\t ").toString());
        assertEquals("!\"name-2\":\"value1\"", append(new StringBuilder("!"), "name-2", "value1").toString());
        assertEquals("\"s\":\"the cow says \\\"moo\"", append(new StringBuilder(), "s", "the cow says \"moo").toString());
        assertEquals("\"1\":\"the cow says \\\"moo\\\"\"", append(new StringBuilder(), "1", "the cow says \"moo\"").toString());
    }

    @Test
    public void escapeBackslashTest() {
        assertEquals("\\\\the cow says moo", escape(new StringBuilder(), "\\the cow says moo").toString());
        assertEquals("the cow says moo\\\\", escape(new StringBuilder(), "the cow says moo\\").toString());
        assertEquals("{the cow \\\\says moo", escape(new StringBuilder("{"), "the cow \\says moo").toString());
    }

    @Test
    public void escapeBackspaceTest() {
        assertEquals("{\\bthe cow says moo", escape(new StringBuilder("{"), "\bthe cow says moo").toString());
        assertEquals("the cow\\b says moo", escape(new StringBuilder(), "the cow\b says moo").toString());
        assertEquals("the cow says moo\\b", escape(new StringBuilder(), "the cow says moo\b").toString());
    }

    @Test
    public void escapeFormFeedTest() {
        assertEquals("\\fthe cow says moo", escape(new StringBuilder(), "\fthe cow says moo").toString());
        assertEquals("the cow\\f says moo", escape(new StringBuilder(), "the cow\f says moo").toString());
        assertEquals("the cow says moo\\f", escape(new StringBuilder(), "the cow says moo\f").toString());
    }

    @Test
    public void escapeNewLineTest() {
        assertEquals("\\nthe cow says moo", escape(new StringBuilder(), "\nthe cow says moo").toString());
        assertEquals("the cow\\n says moo", escape(new StringBuilder(), "the cow\n says moo").toString());
        assertEquals("the cow says moo\\n", escape(new StringBuilder(), "the cow says moo\n").toString());
    }

    @Test
    public void escapeNullsTest() {
        assertEquals("", escape(new StringBuilder(), null).toString());
        assertEquals("ABC", escape(new StringBuilder("ABC"), null).toString());
    }

    @Test
    public void escapeQuoteTest() {
        assertEquals("\\\"the cow says moo", escape(new StringBuilder(), "\"the cow says moo").toString());
        assertEquals("the cow says moo\\\"", escape(new StringBuilder(), "the cow says moo\"").toString());
        assertEquals("the cow says \\\"moo", escape(new StringBuilder(), "the cow says \"moo").toString());
    }

    @Test
    public void escapeReturnTest() {
        assertEquals("\\rthe cow says moo", escape(new StringBuilder(), "\rthe cow says moo").toString());
        assertEquals("the cow\\r says moo", escape(new StringBuilder(), "the cow\r says moo").toString());
        assertEquals("the cow says moo\\r", escape(new StringBuilder(), "the cow says moo\r").toString());
    }

    @Test
    public void escapeTabTest() {
        assertEquals("\\tthe cow says moo", escape(new StringBuilder(), "\tthe cow says moo").toString());
        assertEquals("the cow\\t says moo", escape(new StringBuilder(), "the cow\t says moo").toString());
        assertEquals("the cow says moo\\t", escape(new StringBuilder(), "the cow says moo\t").toString());
    }

    @Test
    public void escapeUnicodeTest() {
        assertEquals("\\u001B", escape(new StringBuilder(), "\u001B").toString());
        assertEquals(" \\u007F", escape(new StringBuilder(" "), "\u007F").toString());
        assertEquals("\\u204B", escape(new StringBuilder(), "\u204B").toString());
        assertEquals("[", escape(new StringBuilder(), "\u005B").toString());
        assertEquals(" ö", escape(new StringBuilder(), " ö").toString());
        assertEquals("  ö has \\ndiaeresis", escape(new StringBuilder(), "  \u00F6 has \ndiaeresis").toString());
        assertEquals("鯨", escape(new StringBuilder(), "\u9BE8").toString());
        assertEquals("鯨 is a whale", escape(new StringBuilder(), "鯨 is a whale").toString());
    }

    @Test
    public void startTest() {
        StringBuilder json = start(new StringBuilder(), "category1", "agent1", "version1", 1455908589662L);
        assertTrue("has category", json.toString().contains("{\"category\":\"category1\","));
        assertTrue("has agent", json.toString().contains("\"agent\":\"agent1\","));
        assertTrue("has version", json.toString().contains("\"version\":\"version1\","));
        assertTrue("has now", json.toString().contains("\"now\":\"1455908589662\""));
    }

    @Test
    public void stopTest() {
        assertEquals("}", stop(new StringBuilder()).toString());
    }

}
