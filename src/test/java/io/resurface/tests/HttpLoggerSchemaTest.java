// © 2016-2020 Resurface Labs Inc.

package io.resurface.tests;

import io.resurface.HttpLogger;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static io.resurface.tests.Helper.parseable;

/**
 * Tests against schemas for HTTP loggers.
 */
public class HttpLoggerSchemaTest {

    @Test
    public void loadsDefaultSchemaTest() {
        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue);
        expect(logger.getSchema()).toBeNull();
    }

    @Test
    public void loadsSchemaTest() {
        String myschema = "type Foo { bar: String }";

        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "include debug", myschema);
        expect(logger.getSchema()).toEqual(myschema);

        expect(queue.size()).toEqual(1);
        String msg = queue.get(0);
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"graphql_schema\",\"" + myschema + "\"]");
    }

    @Test
    public void loadsSchemaFromFileTest() {
        String myschema = "type Query { hello: String }";

        List<String> queue = new ArrayList<>();
        HttpLogger logger = new HttpLogger(queue, "include debug", "file://./test/schema1.txt");
        expect(logger.getSchema()).toStartWith(myschema);

        expect(queue.size()).toEqual(1);
        String msg = queue.get(0);
        expect(parseable(msg)).toBeTrue();
        expect(msg).toContain("[\"graphql_schema\",\"" + myschema + "\"]");
    }

    @Test
    public void raisesExpectedErrorsTest() {
        try {
            List<String> queue = new ArrayList<>();
            HttpLogger logger = new HttpLogger(queue, "include debug", "file://~/bleepblorpbleepblorp12345");
            expect(false).toBeTrue();
        } catch (IllegalArgumentException iae) {
            expect(iae.getMessage()).toEqual("Failed to load schema: ~/bleepblorpbleepblorp12345");
        }
    }

}
