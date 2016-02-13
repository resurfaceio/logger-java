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
    public static String getDefaultURL() {
        return "https://resurfaceio.herokuapp.com/messages";
    }

    /**
     * Returns version number from generated properties file.
     */
    public static String getVersion() {
        try (InputStream is = Logger.class.getResourceAsStream("/version.properties")) {
            Properties p = new Properties();
            p.load(is);
            return p.getProperty("version", null);
        } catch (Exception e) {
            throw new RuntimeException("Version could not be loaded: " + e.getMessage());
        }
    }

    /**
     * Default constructor.
     */
    public Logger() {
        this.url = getDefaultURL();
    }

    /**
     * Constructor with config params.
     */
    public Logger(String url) {
        this.url = url;
    }

    private final String version = getVersion();
    private final String url;

    /**
     * Formats status message.
     */
    public String formatStatus() {
        return "{\"type\":\"STATUS\",\"source\":\"resurfaceio-logger-java\",\"version\":\"" + version + "\",\"now\":\"" + System.currentTimeMillis() + "\"}";
    }

    /**
     * Posts status message.
     */
    public boolean logStatus() {
        return post(formatStatus()) == 200;
    }

    /**
     * Perform POST to url.
     */
    private int post(String body) {
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
            throw new RuntimeException(ioe);
        }
    }

}
