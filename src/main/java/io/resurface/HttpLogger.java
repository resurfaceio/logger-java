// © 2016-2017 Resurface Labs LLC

package io.resurface;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Usage logger for HTTP/HTTPS protocol.
 */
public class HttpLogger extends BaseLogger<HttpLogger> {

    /**
     * Agent string identifying this logger.
     */
    public static final String AGENT = "HttpLogger.java";

    /**
     * Initialize enabled logger using default url.
     */
    public HttpLogger() {
        super(AGENT);
    }

    /**
     * Initialize enabled/disabled logger using default url.
     */
    public HttpLogger(boolean enabled) {
        super(AGENT, enabled);
    }

    /**
     * Initialize enabled logger using url.
     */
    public HttpLogger(String url) {
        super(AGENT, url);
    }

    /**
     * Initialize enabled/disabled logger using url.
     */
    public HttpLogger(String url, boolean enabled) {
        super(AGENT, url, enabled);
    }

    /**
     * Initialize enabled logger using queue.
     */
    public HttpLogger(List<String> queue) {
        super(AGENT, queue);
    }

    /**
     * Initialize enabled/disabled logger using queue.
     */
    public HttpLogger(List<String> queue, boolean enabled) {
        super(AGENT, queue, enabled);
    }

    /**
     * Formats HTTP request and response as JSON message.
     */
    public String format(HttpServletRequest request, HttpServletResponse response) {
        return format(request, response, null, null, System.currentTimeMillis());
    }

    /**
     * Formats HTTP request and response as JSON message.
     */
    public String format(HttpServletRequest request, HttpServletResponse response, String response_body) {
        return format(request, response, response_body, null, System.currentTimeMillis());
    }

    /**
     * Formats HTTP request and response as JSON message.
     */
    public String format(HttpServletRequest request, HttpServletResponse response, String response_body, String request_body) {
        return format(request, response, response_body, request_body, System.currentTimeMillis());
    }

    /**
     * Formats HTTP request and response as JSON message.
     */
    public String format(HttpServletRequest request, HttpServletResponse response,
                         String response_body, String request_body, long now) {
        List<String[]> message = HttpMessageImpl.build(request, response, response_body, request_body);
        message.add(new String[]{"agent", this.agent});
        message.add(new String[]{"version", this.version});
        message.add(new String[]{"now", String.valueOf(now)});
        return Json.stringify(message);
    }

    /**
     * Returns true if content type indicates string data.
     */
    public static boolean isStringContentType(String s) {
        return s != null && STRING_TYPES_REGEX.matcher(s).find();
    }

    /**
     * Logs HTTP request and response to intended destination.
     */
    public boolean log(HttpServletRequest request, HttpServletResponse response) {
        return !isEnabled() || submit(format(request, response, null, null));
    }

    /**
     * Logs HTTP request and response to intended destination.
     */
    public boolean log(HttpServletRequest request, HttpServletResponse response, String response_body) {
        return !isEnabled() || submit(format(request, response, response_body, null));
    }

    /**
     * Logs HTTP request and response to intended destination.
     */
    public boolean log(HttpServletRequest request, HttpServletResponse response, String response_body, String request_body) {
        return !isEnabled() || submit(format(request, response, response_body, request_body));
    }

    private static final String STRING_TYPES = "(?i)^text/(html|plain|xml)|application/(json|soap|xml|x-www-form-urlencoded)";
    private static final Pattern STRING_TYPES_REGEX = Pattern.compile(STRING_TYPES);

}
