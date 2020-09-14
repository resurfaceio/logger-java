// © 2016-2020 Resurface Labs Inc.

package io.resurface;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
     * Initialize logger using default url and default rules.
     */
    public HttpLogger() {
        super(AGENT);
        initialize(null, null);
    }

    /**
     * Initialize enabled/disabled logger using default url and default rules.
     */
    public HttpLogger(boolean enabled) {
        super(AGENT, enabled);
        initialize(null, null);
    }

    /**
     * Initialize logger using specified url and default rules.
     */
    public HttpLogger(String url) {
        super(AGENT, url);
        initialize(null, null);
    }

    /**
     * Initialize logger using specified url and specified rules.
     */
    public HttpLogger(String url, String rules) {
        super(AGENT, url);
        initialize(rules, null);
    }

    /**
     * Initialize logger using specified url, rules and schema.
     */
    public HttpLogger(String url, String rules, String schema) {
        super(AGENT, url);
        initialize(rules, schema);
    }

    /**
     * Initialize enabled/disabled logger using specified url and default rules.
     */
    public HttpLogger(String url, boolean enabled) {
        super(AGENT, url, enabled);
        initialize(null, null);
    }

    /**
     * Initialize enabled/disabled logger using specified url and specified rules.
     */
    public HttpLogger(String url, boolean enabled, String rules) {
        super(AGENT, url, enabled);
        initialize(rules, null);
    }

    /**
     * Initialize enabled/disabled logger using specified url, rules and schema.
     */
    public HttpLogger(String url, boolean enabled, String rules, String schema) {
        super(AGENT, url, enabled);
        initialize(rules, schema);
    }

    /**
     * Initialize enabled logger using queue and default rules.
     */
    public HttpLogger(List<String> queue) {
        super(AGENT, queue);
        initialize(null, null);
    }

    /**
     * Initialize enabled logger using queue and specified rules.
     */
    public HttpLogger(List<String> queue, String rules) {
        super(AGENT, queue);
        initialize(rules, null);
    }

    /**
     * Initialize enabled logger using queue and specified rules/schema.
     */
    public HttpLogger(List<String> queue, String rules, String schema) {
        super(AGENT, queue);
        initialize(rules, schema);
    }

    /**
     * Initialize enabled/disabled logger using queue and default rules.
     */
    public HttpLogger(List<String> queue, boolean enabled) {
        super(AGENT, queue, enabled);
        initialize(null, null);
    }

    /**
     * Initialize enabled/disabled logger using queue and specified rules.
     */
    public HttpLogger(List<String> queue, boolean enabled, String rules) {
        super(AGENT, queue, enabled);
        initialize(rules, null);
    }

    /**
     * Initialize enabled/disabled logger using queue and specified rules/schema.
     */
    public HttpLogger(List<String> queue, boolean enabled, String rules, String schema) {
        super(AGENT, queue, enabled);
        initialize(rules, schema);
    }

    /**
     * Initialize a new logger.
     */
    private void initialize(String rules, String schema) {
        // parse specified rules
        this.rules = new HttpRules(rules);

        // apply configuration rules
        this.skip_compression = this.rules.skip_compression;
        this.skip_submission = this.rules.skip_submission;
        if ((url != null) && (url.startsWith("http:") && !this.rules.allow_http_url)) {
            this.enableable = false;
            this.enabled = false;
        }

        // load schema if present
        boolean schema_exists = (schema != null);
        if (schema_exists) {
            if (schema.startsWith("file://")) {
                String rfile = schema.substring(7).trim();
                try {
                    this.schema = new String(Files.readAllBytes(Paths.get(rfile)));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to load schema: " + rfile);
                }
            } else {
                this.schema = schema;
            }
        } else {
            this.schema = null;
        }

        // submit metadata message
        if (this.enabled) {
            List<String[]> details = new ArrayList<>();
            details.add(new String[]{"message_type", "metadata"});
            details.add(new String[]{"agent", this.agent});
            details.add(new String[]{"host", this.host});
            details.add(new String[]{"version", this.version});
            details.add(new String[]{"metadata_id", this.metadataId});
            if (schema_exists) details.add(new String[]{"graphql_schema", this.schema});
            submit(Json.stringify(details));
        }
    }

    /**
     * Returns rules specified when creating this logger.
     */
    public HttpRules getRules() {
        return rules;
    }

    /**
     * Returns schema specified when creating this logger.
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Apply logging rules to message details and submit JSON message.
     */
    public void submitIfPassing(List<String[]> details) {
        details = rules.apply(details);
        if (details == null) return;
        details.add(new String[]{"metadata_id", this.metadataId});
        submit(Json.stringify(details));
    }

    /**
     * Returns true if content type indicates string data.
     */
    public static boolean isStringContentType(String s) {
        return s != null && STRING_TYPES_REGEX.matcher(s).find();
    }

    protected HttpRules rules;
    protected String schema;
    protected static final String STRING_TYPES = "(?i)^text/(html|plain|xml)|application/(json|soap|xml|x-www-form-urlencoded)";
    protected static final Pattern STRING_TYPES_REGEX = Pattern.compile(STRING_TYPES);

}
