// © 2016-2020 Resurface Labs Inc.

package io.resurface;

/**
 * Utilities for all usage loggers.
 */
public final class UsageLoggers {

    private static final boolean BRICKED = "true".equals(System.getenv("USAGE_LOGGERS_DISABLE"));

    private static boolean disabled = BRICKED;

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
        if (!BRICKED) disabled = false;
    }

    /**
     * Returns true if usage loggers are generally enabled.
     */
    public static boolean isEnabled() {
        return !disabled;
    }

    /**
     * Returns url to use by default.
     */
    public static String urlByDefault() {
        String url = System.getProperty("USAGE_LOGGERS_URL");
        return (url == null) ? System.getenv("USAGE_LOGGERS_URL") : url;
    }

}
