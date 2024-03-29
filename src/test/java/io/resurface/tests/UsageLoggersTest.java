// © 2016-2024 Graylog, Inc.

package io.resurface.tests;

import io.resurface.UsageLoggers;
import org.junit.Test;

import static com.mscharhag.oleaster.matcher.Matchers.expect;

/**
 * Tests against utilities for all usage loggers.
 */
public class UsageLoggersTest {

    @Test
    public void providesDefaultUrlTest() {
        String url = UsageLoggers.urlByDefault();
        expect(url).toBeNull();
    }

}
