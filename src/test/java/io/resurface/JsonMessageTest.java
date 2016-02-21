// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import org.junit.Test;

import static io.resurface.JsonMessage.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests against utility methods for formatting JSON messages.
 */
public class JsonMessageTest {

    @Test
    public void appendTest() {
        assertEquals("\"name1\":123", append(new StringBuilder(), "name1", 123).toString());
        assertEquals("\"1name1\":1455908665227", append(new StringBuilder(), "1name1", 1455908665227L).toString());
        assertEquals("\"name2\":\"value1\"", append(new StringBuilder(), "name2", "value1").toString());
        assertEquals("\"sand_castle\":\"the cow says \\\"moo\"", append(new StringBuilder(), "sand_castle", "the cow says \"moo").toString());
        assertEquals("\"Sand-Castle\":\"the cow says \\\"moo\\\"\"", append(new StringBuilder(), "Sand-Castle", "the cow says \"moo\"").toString());
    }

    @Test
    public void escapeTest() {
        assertEquals("\\\\the cow says moo", escape(new StringBuilder(), "\\the cow says moo").toString());
        assertEquals("the cow says moo\\\\", escape(new StringBuilder(), "the cow says moo\\").toString());
        assertEquals("the cow \\\\says moo", escape(new StringBuilder(), "the cow \\says moo").toString());
        assertEquals("\\\"the cow says moo", escape(new StringBuilder(), "\"the cow says moo").toString());
        assertEquals("the cow says moo\\\"", escape(new StringBuilder(), "the cow says moo\"").toString());
        assertEquals("the cow says \\\"moo", escape(new StringBuilder(), "the cow says \"moo").toString());
    }

    @Test
    public void finishTest() {
        assertEquals("}", finish(new StringBuilder()).toString());
    }

    @Test
    public void startTest() {
        StringBuilder json = start(new StringBuilder(), "category1", "source1", "version1", 1455908589662L);
        assertTrue("has category", json.toString().contains("{\"category\":\"category1\","));
        assertTrue("has source", json.toString().contains("\"source\":\"source1\","));
        assertTrue("has version", json.toString().contains("\"version\":\"version1\","));
        assertTrue("has now", json.toString().contains("\"now\":1455908589662"));
    }

}
