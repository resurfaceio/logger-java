// © 2016-2017 Resurface Labs LLC

package io.resurface.tests;

import org.junit.Test;

import static io.resurface.tests.Helper.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for mock objects and utilities for testing.
 */
public class HelperTest {

    @Test
    public void detectsGoodJsonTest() {
        assertTrue("case 1", parseable("{}"));
        assertTrue("case 2", parseable("{ }"));
        assertTrue("case 3", parseable("{\n}"));
        assertTrue("case 4", parseable("{\n\n\n}"));
        assertTrue("case 5", parseable(MOCK_JSON));
    }

    @Test
    public void detectsInvalidJsonTest() {
        assertFalse("case 00", parseable(null));
        assertFalse("case 01", parseable(""));
        assertFalse("case 02", parseable(" "));
        assertFalse("case 03", parseable("\n\n\n\n"));
        assertFalse("case 04", parseable("1234"));
        assertFalse("case 05", parseable("archer"));
        assertFalse("case 06", parseable("\"sterling archer\""));
        assertFalse("case 07", parseable("[]"));
        assertFalse("case 08", parseable("[,]"));
        assertFalse("case 09", parseable("[:,]"));
        assertFalse("case 10", parseable("[ ]"));
        assertFalse("case 11", parseable(","));
        assertFalse("case 12", parseable("{"));
        assertFalse("case 13", parseable("{,"));
        assertFalse("case 14", parseable(",,"));
        assertFalse("case 15", parseable("{{"));
        assertFalse("case 16", parseable("{{,,"));
        assertFalse("case 17", parseable("}"));
        assertFalse("case 18", parseable(",}"));
        assertFalse("case 19", parseable("},"));
        assertFalse("case 20", parseable(",},"));
        assertFalse("case 21", parseable("{{}"));
        assertFalse("case 22", parseable("{,}"));
        assertFalse("case 23", parseable("{,,}"));
        assertFalse("case 24", parseable("exact words"));
        assertFalse("case 25", parseable("his exact words"));
        assertFalse("case 26", parseable("\"exact words"));
        assertFalse("case 27", parseable("his exact words\""));
        assertFalse("case 28", parseable("\"hello\":\"world\" }"));
        assertFalse("case 29", parseable("{ \"hello\":\"world\""));
        assertFalse("case 30", parseable("{ \"hello world\"}"));
        assertFalse("case 31", parseable("{ \"hello\" world\"}"));
        assertFalse("case 32", parseable("{ \"hello \"world\"}"));
        assertFalse("case 33", parseable("{ \"hello world\":}"));
        assertFalse("case 34", parseable("{ \"hello\"\"world\" }"));
        assertFalse("case 35", parseable("{ \"hello\"\"world\", }"));
        assertFalse("case 36", parseable("{ ,\"hello\"\"world\" }"));
        assertFalse("case 37", parseable("{ ,\"hello\"\"world\", }"));
        assertFalse("case 38", parseable("{ \"hello\":\"world\"\"hello\":\"world\" }"));
        assertFalse("case 39", parseable("{ ,\"hello\":\"world\"\"hello\":\"world\" }"));
        assertFalse("case 40", parseable("{ \"hello\":\"world\"\"hello\":\"world\", }"));
        assertFalse("case 41", parseable("{ [ \"hello\":\"world\" }"));
        assertFalse("case 42", parseable("{ [ \"hello\":\"world\",] }"));
        assertFalse("case 43", parseable("{ [ \"hello\":\"world\" ], }"));
        assertFalse("case 44", parseable("{ [ \"hello\":\"world\" ] \"hello\":\"world\" }"));
        assertFalse("case 45", parseable(MOCK_JSON_ESCAPED));
        assertFalse("case 46", parseable(MOCK_HTML));
        assertFalse("case 47", parseable(MOCK_HTML_ESCAPED));
    }

}
