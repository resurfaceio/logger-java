// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface.tests;

import io.resurface.HttpLoggerFactory;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests against factory for HTTP usage loggers.
 */
public class HttpLoggerFactoryTest {

    @Test
    public void defaultLoggerTest() {
        assertTrue("consistent logger", HttpLoggerFactory.get() == HttpLoggerFactory.get());
        HttpLoggerFactory.get().disable();
        assertTrue("logger disabled", !HttpLoggerFactory.get().isEnabled());
        HttpLoggerFactory.get().enable();
        assertTrue("logger enabled", HttpLoggerFactory.get().isEnabled());
    }

}
