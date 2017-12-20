// © 2016-2017 Resurface Labs LLC

package io.resurface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.zip.DeflaterOutputStream;

/**
 * Basic usage logger to embed or extend.
 */
public class BaseLogger<T extends BaseLogger> {

    /**
     * Initialize enabled logger using default url.
     */
    public BaseLogger(String agent) {
        this(agent, true);
    }

    /**
     * Initialize enabled/disabled logger using default url.
     */
    public BaseLogger(String agent, boolean enabled) {
        this(agent, UsageLoggers.urlByDefault(), enabled);
    }

    /**
     * Initialize enabled logger using url.
     */
    public BaseLogger(String agent, String url) {
        this(agent, url, true);
    }

    /**
     * Initialize enabled/disabled logger using url.
     */
    public BaseLogger(String agent, String url, boolean enabled) {
        this.agent = agent;
        this.version = version_lookup();
        this.queue = null;

        // set options in priority order
        this.enabled = enabled;
        if (url == null) {
            this.url = UsageLoggers.urlByDefault();
            if (this.url == null) this.enabled = false;
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
        this.enableable = (this.url != null);
    }

    /**
     * Initialize enabled logger using queue.
     */
    public BaseLogger(String agent, List<String> queue) {
        this(agent, queue, true);
    }

    /**
     * Initialize enabled/disabled logger using queue.
     */
    public BaseLogger(String agent, List<String> queue, boolean enabled) {
        this.agent = agent;
        this.version = version_lookup();
        this.enabled = enabled;
        this.queue = queue;
        this.url = null;
        this.enableable = (this.queue != null);
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
        if (enableable) enabled = true;
        return (T) this;
    }

    /**
     * Returns agent string identifying this logger.
     */
    public String getAgent() {
        return agent;
    }

    /**
     * Returns true if message compression is being skipped.
     */
    public boolean getSkipCompression() {
        return skip_compression;
    }

    /**
     * Returns true if message submission is being skipped.
     */
    public boolean getSkipSubmission() {
        return skip_submission;
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
     * Returns true if this logger can ever be enabled.
     */
    public boolean isEnableable() {
        return enableable;
    }

    /**
     * Returns true if this logger is currently enabled.
     */
    public boolean isEnabled() {
        return enabled && UsageLoggers.isEnabled();
    }

    /**
     * Sets if message compression will be skipped.
     */
    public void setSkipCompression(boolean skip_compression) {
        this.skip_compression = skip_compression;
    }

    /**
     * Sets if message submission will be skipped.
     */
    public void setSkipSubmission(boolean skip_submission) {
        this.skip_submission = skip_submission;
    }

    /**
     * Submits JSON message to intended destination.
     */
    public boolean submit(String json) {
        if (this.skip_submission || !isEnabled()) {
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
                if (!this.skip_compression) url_connection.setRequestProperty("Content-Encoding", "deflated");
                try (OutputStream os = url_connection.getOutputStream()) {
                    if (this.skip_compression) {
                        os.write(json.getBytes());
                    } else {
                        try (DeflaterOutputStream dos = new DeflaterOutputStream(os, true)) {
                            dos.write(json.getBytes());
                            dos.finish();
                            dos.flush();
                        }
                    }
                    os.flush();
                }
                return url_connection.getResponseCode() == 204;
            } catch (IOException ioe) {
                return false;
            }
        }
    }

    /**
     * Retrieves version number from runtime properties file.
     */
    public static String version_lookup() {
        try (InputStream is = BaseLogger.class.getResourceAsStream("/version.properties")) {
            Properties p = new Properties();
            p.load(is);
            return p.getProperty("version", null);
        } catch (Exception e) {
            throw new RuntimeException("Version could not be loaded: " + e.getMessage());
        }
    }

    protected final String agent;
    protected final boolean enableable;
    protected boolean enabled;
    protected final List<String> queue;
    protected boolean skip_compression = false;
    protected boolean skip_submission = false;
    protected String url;
    protected final String version;

}
