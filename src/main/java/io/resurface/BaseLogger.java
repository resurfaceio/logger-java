// © 2016-2021 Resurface Labs Inc.

package io.resurface;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
        this.host = host_lookup();
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
                this.url_parsed = new URL(this.url);
                if (!this.url_parsed.getProtocol().contains("http")) throw new RuntimeException();
            } catch (Exception e) {
                this.url = null;
                this.url_parsed = null;
                this.enabled = false;
            }
        }

        // finalize internal properties
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
        this.host = host_lookup();
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
     * Returns cached host identifier.
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns queue destination where messages are sent.
     */
    public List<String> getQueue() {
        return queue;
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
    public void submit(String msg) {
        if (msg == null || this.skip_submission || !isEnabled()) {
            // do nothing
        } else if (queue != null) {
            queue.add(msg);
            submit_successes.incrementAndGet();
        } else {
            try {
                HttpURLConnection url_connection = (HttpURLConnection) this.url_parsed.openConnection();
                url_connection.setConnectTimeout(5000);
                url_connection.setReadTimeout(1000);
                url_connection.setRequestMethod("POST");
                url_connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                url_connection.setRequestProperty("User-Agent", "Resurface/" + version + " (Java)");
                url_connection.setDoOutput(true);
                if (!this.skip_compression) url_connection.setRequestProperty("Content-Encoding", "deflated");
                try (OutputStream os = url_connection.getOutputStream()) {
                    if (this.skip_compression) {
                        os.write(msg.getBytes(StandardCharsets.UTF_8));
                    } else {
                        try (DeflaterOutputStream dos = new DeflaterOutputStream(os, true)) {
                            dos.write(msg.getBytes(StandardCharsets.UTF_8));
                            dos.finish();
                            dos.flush();
                        }
                    }
                    os.flush();
                }
                if (url_connection.getResponseCode() == 204) {
                    submit_successes.incrementAndGet();
                } else {
                    submit_failures.incrementAndGet();
                }
            } catch (Exception e) {
                submit_failures.incrementAndGet();
            }
        }
    }

    /**
     * Returns count of submissions that failed.
     */
    public int getSubmitFailures() {
        return submit_failures.get();
    }

    /**
     * Returns count of submissions that succeeded.
     */
    public int getSubmitSuccesses() {
        return submit_successes.get();
    }

    /**
     * Returns host identifier for this logger.
     */
    public static String host_lookup() {
        String dyno = System.getenv("DYNO");
        if (dyno != null) return dyno;
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Returns version number for this logger.
     */
    public static String version_lookup() {
        return "2.1.0";
    }

    protected final String agent;
    protected boolean enableable;
    protected boolean enabled;
    protected final String host;
    protected final List<String> queue;
    protected boolean skip_compression = false;
    protected boolean skip_submission = false;
    protected final AtomicInteger submit_failures = new AtomicInteger();
    protected final AtomicInteger submit_successes = new AtomicInteger();
    protected String url;
    protected URL url_parsed;
    protected final String version;

}
