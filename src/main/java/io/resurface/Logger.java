// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import static io.resurface.Message.*;

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
     * Formats JSON message for simple echo.
     */
    public StringBuilder formatEcho(StringBuilder json, long now) {
        define(json, "echo", version, now);
        return finish(json);
    }

    /**
     * Formats JSON message for HTTP request.
     */
    public StringBuilder formatHttpRequest(StringBuilder json, long now, HttpServletRequest request) {
        define(json, "http_request", version, now).append(',');
        append(json, "url", request.getRequestURL());
        return finish(json);
    }

    /**
     * Formats JSON message for HTTP response.
     */
    public StringBuilder formatHttpResponse(StringBuilder json, long now, HttpServletResponse response) {
        define(json, "http_response", version, now).append(',');
        append(json, "code", response.getStatus());
        return finish(json);
    }

    /**
     * Logs echo (in JSON format) to remote url.
     */
    public boolean logEcho() {
        StringBuilder json = new StringBuilder(64);
        formatEcho(json, System.currentTimeMillis());
        return post(json.toString()) == 200;
    }

    /**
     * Logs http request (in JSON format) to remote url.
     */
    public boolean logHttpRequest(HttpServletRequest request) {
        StringBuilder json = new StringBuilder(1024);
        formatHttpRequest(json, System.currentTimeMillis(), request);
        return post(json.toString()) == 200;
    }

    /**
     * Logs http response (in JSON format) to remote url.
     */
    public boolean logHttpResponse(HttpServletResponse response) {
        StringBuilder json = new StringBuilder(1024);
        formatHttpResponse(json, System.currentTimeMillis(), response);
        return post(json.toString()) == 200;
    }

    /**
     * Logs message (via HTTP post) to remote url.
     */
    public int post(String message) {
        try {
            URL url = new URL(this.url);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(1000);
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                os.write(message.getBytes());
                os.flush();
            }
            return con.getResponseCode();
        } catch (IOException ioe) {
            return 404;
        }
    }

    /**
     * Returns remote url where messages are sent.
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
        try (InputStream is = Logger.class.getResourceAsStream("/version.properties")) {
            Properties p = new Properties();
            p.load(is);
            return p.getProperty("version", null);
        } catch (Exception e) {
            throw new RuntimeException("Version could not be loaded: " + e.getMessage());
        }
    }

}
