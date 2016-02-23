// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

/**
 * Factory for HTTP usage loggers.
 */
public final class HttpLoggerFactory {

    private static final HttpLogger default_logger = new HttpLogger();

    /**
     * Returns cached default HTTP logger.
     */
    public static HttpLogger get() {
        return default_logger;
    }

}
