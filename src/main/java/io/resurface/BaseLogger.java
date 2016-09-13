// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * Abstract base class for all usage loggers, with type-safe chaining methods.
 */
public abstract class BaseLogger<T extends BaseLogger> {

    /**
     * Initialize enabled logger using default url.
     */
    protected BaseLogger(String agent) {
        this(agent, true);
    }

    /**
     * Initialize enabled/disabled logger using default url.
     */
    protected BaseLogger(String agent, boolean enabled) {
        this(agent, UsageLoggers.urlByDefault(), enabled);
    }

    /**
     * Initialize enabled logger using url.
     */
    protected BaseLogger(String agent, String url) {
        this(agent, url, true);
    }

    /**
     * Initialize enabled/disabled logger using url.
     */
    protected BaseLogger(String agent, String url, boolean enabled) {
        this.agent = agent;
        this.version = version_lookup();
        this.queue = null;

        // set options in priority order
        this.enabled = enabled;
        if (url == null) {
            this.url = UsageLoggers.urlByDefault();
            if (this.url == null) this.enabled = false;
        } else if (url.equals("DEMO")) {
            this.url = UsageLoggers.urlForDemo();
        } else {
            this.url = url;
        }

        // validate url when present
        if (this.url != null) {
            try {
                if (!new URL(this.url).getProtocol().contains("http")) throw new RuntimeException();
            } catch (Exception e) {
                this.url = null;
                this.enabled = false;
            }
        }
    }

    /**
     * Initialize enabled logger using queue.
     */
    protected BaseLogger(String agent, List<String> queue) {
        this(agent, queue, true);
    }

    /**
     * Initialize enabled/disabled logger using queue.
     */
    protected BaseLogger(String agent, List<String> queue, boolean enabled) {
        this.agent = agent;
        this.version = version_lookup();
        this.enabled = enabled;
        this.queue = queue;
        this.url = null;
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
        if ((queue != null) || (url != null)) enabled = true;
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
     * Returns true if this logger is enabled.
     */
    public boolean isEnabled() {
        return enabled && UsageLoggers.isEnabled();
    }

    /**
     * Submits JSON message to intended destination.
     */
    public boolean submit(String json) {
        if (!isEnabled()) {
            return true;
        } else if (queue != null) {
            queue.add(json);
            return true;
        } else {
            try {
                URL url_parsed = new URL(this.url);
                HttpURLConnection url_connection = (HttpURLConnection) url_parsed.openConnection();
                url_connection.setConnectTimeout(5000);
                url_connection.setReadTimeout(1000);
                url_connection.setRequestMethod("POST");
                url_connection.setDoOutput(true);
                try (OutputStream os = url_connection.getOutputStream()) {
                    os.write(json.getBytes());
                    os.flush();
                }
                return url_connection.getResponseCode() == 200;
            } catch (IOException ioe) {
                // todo retry?
                return false;
            }
        }
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
    protected final List<String> queue;
    protected String url;
    protected final String version;

}
