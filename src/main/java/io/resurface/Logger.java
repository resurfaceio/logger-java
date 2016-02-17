// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

/**
 * Java library for usage logging.
 */
public class Logger {

    /**
     * Default destination for log messages.
     */
    public static final String DEFAULT_URL = "https://resurfaceio.herokuapp.com/messages";

    /**
     * Initialize using defaults.
     */
    public Logger() {
        this.url = DEFAULT_URL;
        this.version = version_lookup();
    }

    /**
     * Initialize using specified params.
     */
    public Logger(String url) {
        this.url = url;
        this.version = version_lookup();
    }

    private final String url;
    private final String version;

    /**
     * Formats status message.
     */
    public String formatStatus(long now) {
        return "{\"type\":\"status\",\"source\":\"resurfaceio-logger-java\",\"version\":\"" + version + "\",\"now\":\"" + now + "\"}";
    }

    /**
     * Posts status message.
     */
    public boolean logStatus() {
        return post(formatStatus(System.currentTimeMillis())) == 200;
    }

    /**
     * Post raw body to url.
     */
    public int post(String body) {
        try {
            URL url = new URL(this.url);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(1000);
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                os.write(body.getBytes());
                os.flush();
            }
            return con.getResponseCode();
        } catch (IOException ioe) {
            return 404;
        }
    }

    /**
     * Returns url destination.
     */
    public String url() {
        return url;
    }

    /**
     * Returns version number.
     */
    public String version() {
        return version;
    }

    /**
     * Returns version number from generated properties file.
     */
    public static String version_lookup() {
        try (InputStream is = Logger.class.getResourceAsStream("/version.properties")) {
            Properties p = new Properties();
            p.load(is);
            return p.getProperty("version", null);
        } catch (Exception e) {
            throw new RuntimeException("Version could not be loaded: " + e.getMessage());
        }
    }

}
