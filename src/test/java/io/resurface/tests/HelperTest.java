// © 2016-2021 Resurface Labs Inc.

package io.resurface.tests;

import org.junit.Test;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static io.resurface.tests.Helper.parseable;

/**
 * Tests for mock objects and utilities for testing.
 */
public class HelperTest {

    @Test
    public void detectsGoodJsonTest() {
        expect(parseable("[ ]")).toBeTrue();
        expect(parseable("[\n]")).toBeTrue();
        expect(parseable("[\n\t\n]")).toBeTrue();
        expect(parseable("[\"A\"]")).toBeTrue();
        expect(parseable("[\"A\",\"B\"]")).toBeTrue();
    }

    @Test
    public void detectsInvalidJsonTest() {
        expect(parseable(null)).toBeFalse();
        expect(parseable("")).toBeFalse();
        expect(parseable(" ")).toBeFalse();
        expect(parseable("\n\n\n\n")).toBeFalse();
        expect(parseable("1234")).toBeFalse();
        expect(parseable("archer")).toBeFalse();
        expect(parseable("\"sterling archer\"")).toBeFalse();
        expect(parseable(",,")).toBeFalse();
        expect(parseable("[]")).toBeFalse();
        expect(parseable("[,,]")).toBeFalse();
        expect(parseable("[\"]")).toBeFalse();
        expect(parseable("[:,]")).toBeFalse();
        expect(parseable(",")).toBeFalse();
        expect(parseable("exact words")).toBeFalse();
        expect(parseable("his exact words")).toBeFalse();
        expect(parseable("\"exact words")).toBeFalse();
        expect(parseable("his exact words\"")).toBeFalse();
        expect(parseable("\"hello\":\"world\" }")).toBeFalse();
        expect(parseable("{ \"hello\":\"world\"")).toBeFalse();
        expect(parseable("{ \"hello world\"}")).toBeFalse();
    }

}
