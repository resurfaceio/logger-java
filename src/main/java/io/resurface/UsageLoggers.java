// © 2016-2017 Resurface Labs LLC

package io.resurface;

/**
 * Utilities for all usage loggers.
 */
public final class UsageLoggers {

    private static final boolean DISABLED = "true".equals(System.getenv("USAGE_LOGGERS_DISABLE"));

    private static boolean disabled = DISABLED;

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
        if (!DISABLED) disabled = false;
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
        return System.getenv("USAGE_LOGGERS_URL");
    }

    /**
     * Returns url for official demo.
     */
    public static String urlForDemo() {
        return "https://demo-resurfaceio.herokuapp.com/messages";
    }

}
