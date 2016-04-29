// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Abstract base class for all usage loggers, with type-safe chaining methods.
 */
public abstract class UsageLogger<T extends UsageLogger> {

    /**
     * URL destination for log messages unless overridden.
     */
    public static final String DEFAULT_URL = "https://resurfaceio.herokuapp.com/messages";

    /**
     * Initialize enabled logger using default url.
     */
    public UsageLogger() {
        this.enabled = true;
        this.url = DEFAULT_URL;
        this.version = version_lookup();
    }

    /**
     * Initialize enabled logger using custom url.
     */
    public UsageLogger(String url) {
        this.enabled = true;
        this.url = url;
        this.version = version_lookup();
    }

    /**
     * Initialize enabled or disabled logger using custom url.
     */
    public UsageLogger(String url, boolean enabled) {
        this.enabled = enabled;
        this.url = url;
        this.version = version_lookup();
    }

    /**
     * Returns agent string identifying this logger.
     */
    public abstract String agent();

    /**
     * Disable this logger.
     */
    public T disable() {
        enabled = false;
        return (T) this;
    }

    /**
     * Enable this logger.
     */
    public T enable() {
        enabled = true;
        return (T) this;
    }

    /**
     * Returns true if this logger is enabled or tracing.
     */
    public boolean isActive() {
        return enabled || tracing;
    }

    /**
     * Returns true if this logger is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns true if keeping a copy of all posted messages.
     */
    public boolean isTracing() {
        return tracing;
    }

    /**
     * Returns unmodifiable copy of recently posted messages.
     */
    public List<String> tracingHistory() {
        return Collections.unmodifiableList(tracing_history);
    }

    /**
     * Starts keeping copy of all posted messages.
     */
    public T tracingStart() {
        tracing = true;
        tracing_history.clear();
        return (T) this;
    }

    /**
     * Stops tracing and clears current tracing history.
     */
    public T tracingStop() {
        tracing = false;
        tracing_history.clear();
        return (T) this;
    }

    /**
     * Returns url destination where messages are sent.
     */
    public String url() {
        return url;
    }

    /**
     * Returns cached version number.
     */
    public String version() {
        return version;
    }

    /**
     * Retrieves version number from runtime properties file.
     */
    public static String version_lookup() {
        try (InputStream is = HttpLogger.class.getResourceAsStream("/version.properties")) {
            Properties p = new Properties();
            p.load(is);
            return p.getProperty("version", null);
        } catch (Exception e) {
            throw new RuntimeException("Version could not be loaded: " + e.getMessage());
        }
    }

    /**
     * Logs message (via HTTP post) to remote url.
     */
    protected boolean post(String json) {
        if (tracing) {
            tracing_history.add(json);
            return true;
        } else {
            try {
                URL url = new URL(this.url);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(5000);
                con.setReadTimeout(1000);
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    os.write(json.getBytes());
                    os.flush();
                }
                return con.getResponseCode() == 200;
            } catch (IOException ioe) {
                return false;
            }
        }
    }

    protected boolean enabled;
    protected boolean tracing = false;
    protected final List<String> tracing_history = new ArrayList<>();
    protected final String url;
    protected final String version;

}
