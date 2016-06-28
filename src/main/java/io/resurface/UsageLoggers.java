// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

/**
 * Utilities for all usage loggers.
 */
public final class UsageLoggers {

    private static boolean disabled = "true".equals(System.getenv("USAGE_LOGGERS_DISABLE"));

    /**
     * Disable all usage loggers.
     */
    public static void disable() {
        disabled = true;
    }

    /**
     * Enable all usage loggers, except those explicitly disabled.
     */
    public static void enable() {
        if (!"true".equals(System.getenv("USAGE_LOGGERS_DISABLE"))) disabled = false;
    }

    /**
     * Returns true if usage loggers are generally enabled.
     */
    public static boolean isEnabled() {
        return !disabled;
    }

    /**
     * Returns url for official demo.
     */
    public static String urlForDemo() {
        return "https://demo-resurfaceio.herokuapp.com/messages";
    }

}
