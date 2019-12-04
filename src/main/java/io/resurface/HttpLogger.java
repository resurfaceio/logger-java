// © 2016-2019 Resurface Labs Inc.

package io.resurface;

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
     * Returns true if content type indicates string data.
     */
    public static boolean isStringContentType(String s) {
        return s != null && STRING_TYPES_REGEX.matcher(s).find();
    }

    private static final String STRING_TYPES = "(?i)^text/(html|plain|xml)|application/(json|soap|xml|x-www-form-urlencoded)";
    private static final Pattern STRING_TYPES_REGEX = Pattern.compile(STRING_TYPES);

    /**
     * Initialize logger using default url and default rules.
     */
    public HttpLogger() {
        super(AGENT);
        initialize(null);
    }

    /**
     * Initialize enabled/disabled logger using default url and default rules.
     */
    public HttpLogger(boolean enabled) {
        super(AGENT, enabled);
        initialize(null);
    }

    /**
     * Initialize logger using specified url and default rules.
     */
    public HttpLogger(String url) {
        super(AGENT, url);
        initialize(null);
    }

    /**
     * Initialize logger using specified url and specified rules.
     */
    public HttpLogger(String url, String rules) {
        super(AGENT, url);
        initialize(rules);
    }

    /**
     * Initialize enabled/disabled logger using specified url and default rules.
     */
    public HttpLogger(String url, boolean enabled) {
        super(AGENT, url, enabled);
        initialize(null);
    }

    /**
     * Initialize enabled/disabled logger using specified url and specified rules.
     */
    public HttpLogger(String url, boolean enabled, String rules) {
        super(AGENT, url, enabled);
        initialize(rules);
    }

    /**
     * Initialize enabled logger using queue and default rules.
     */
    public HttpLogger(List<String> queue) {
        super(AGENT, queue);
        initialize(null);
    }

    /**
     * Initialize enabled logger using queue and specified rules.
     */
    public HttpLogger(List<String> queue, String rules) {
        super(AGENT, queue);
        initialize(rules);
    }

    /**
     * Initialize enabled/disabled logger using queue and default rules.
     */
    public HttpLogger(List<String> queue, boolean enabled) {
        super(AGENT, queue, enabled);
        initialize(null);
    }

    /**
     * Initialize enabled/disabled logger using queue and specified rules.
     */
    public HttpLogger(List<String> queue, boolean enabled, String rules) {
        super(AGENT, queue, enabled);
        initialize(rules);
    }

    /**
     * Initialize a new logger.
     */
    private void initialize(String rules) {
        // parse specified rules
        this.rules = new HttpRules(rules);

        // apply configuration rules
        this.skip_compression = this.rules.skip_compression;
        this.skip_submission = this.rules.skip_submission;
        if ((url != null) && (url.startsWith("http:") && !this.rules.allow_http_url)) {
            this.enableable = false;
            this.enabled = false;
        }
    }

    private HttpRules rules;

    /**
     * Returns rules specified when creating this logger.
     */
    public HttpRules getRules() {
        return rules;
    }

    /**
     * Apply logging rules to message details and submit JSON message.
     */
    public void submitIfPassing(List<String[]> details) {
        try {
            // apply active rules
            details = rules.apply(details);
            if (details == null) return;

            // finalize message
            details.add(new String[]{"agent", this.agent});
            details.add(new String[]{"host", this.host});
            details.add(new String[]{"version", this.version});

            // let's do this thing
            submit(Json.stringify(details));
        } catch (Exception e) {
            // todo count as error
        }
    }

}
