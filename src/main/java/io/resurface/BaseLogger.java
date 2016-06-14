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
public abstract class BaseLogger<T extends BaseLogger> {

    /**
     * URL destination for log messages unless overridden.
     */
    public static final String DEFAULT_URL = "https://resurfaceio.herokuapp.com/messages";

    /**
     * Initialize enabled logger using default url.
     */
    protected BaseLogger(String agent) {
        this(agent, DEFAULT_URL, true);
    }

    /**
     * Initialize enabled logger using custom url.
     */
    protected BaseLogger(String agent, String url) {
        this(agent, url, true);
    }

    /**
     * Initialize enabled or disabled logger using custom url.
     */
    protected BaseLogger(String agent, String url, boolean enabled) {
        this.agent = agent;
        this.enabled = enabled;
        this.url = url;
        this.version = version_lookup();
    }

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
     * Returns agent string identifying this logger.
     */
    public String getAgent() {
        return agent;
    }

    /**
     * Returns url destination where messages are sent.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns cached version number.
     */
    public String getVersion() {
        return version;
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
     * Submits JSON message to intended destination.
     */
    public boolean submit(String json) {
        if (tracing) {
            tracing_history.add(json);
            return true;
        } else if (enabled) {
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
        return true;
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

    protected final String agent;
    protected boolean enabled;
    protected boolean tracing = false;
    protected final List<String> tracing_history = new ArrayList<>();
    protected final String url;
    protected final String version;

}
