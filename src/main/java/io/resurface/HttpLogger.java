// © 2016-2018 Resurface Labs LLC

package io.resurface;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * Usage logger for HTTP/HTTPS protocol.
 */
public class HttpLogger extends BaseLogger<HttpLogger> {

    /**
     * Agent string identifying this logger.
     */
    public static final String AGENT = "HttpLogger.java";

    /**
     * Returns rules used by default when none are declared.
     */
    public static String getDefaultRules() {
        return defaultRules;
    }

    /**
     * Updates rules used by default when none are declared.
     */
    public static void setDefaultRules(String r) {
        defaultRules = r.replaceAll("(?m)^\\s*include default\\s*$", "");
    }

    private static String defaultRules = HttpRules.getStrictRules();

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
     * Initialize logger using specified rules.
     */
    private void initialize(String rules) {
        // read rules from param or defaults
        if (rules != null) {
            this.rules = rules.replaceAll("(?m)^\\s*include default\\s*$", Matcher.quoteReplacement(defaultRules));
        } else {
            this.rules = defaultRules;
        }

        // parse and break out rules by verb
        List<HttpRule> prs = HttpRules.parse(this.rules);
        this.rules_allow_http_url = prs.stream().anyMatch(r -> "allow_http_url".equals(r.verb));
        this.rules_copy_session_field = prs.stream().filter(r -> "copy_session_field".equals(r.verb)).collect(toList());
        this.rules_remove = prs.stream().filter(r -> "remove".equals(r.verb)).collect(toList());
        this.rules_remove_if = prs.stream().filter(r -> "remove_if".equals(r.verb)).collect(toList());
        this.rules_remove_if_found = prs.stream().filter(r -> "remove_if_found".equals(r.verb)).collect(toList());
        this.rules_remove_unless = prs.stream().filter(r -> "remove_unless".equals(r.verb)).collect(toList());
        this.rules_remove_unless_found = prs.stream().filter(r -> "remove_unless_found".equals(r.verb)).collect(toList());
        this.rules_replace = prs.stream().filter(r -> "replace".equals(r.verb)).collect(toList());
        this.rules_sample = prs.stream().filter(r -> "sample".equals(r.verb)).collect(toList());
        this.rules_stop = prs.stream().filter(r -> "stop".equals(r.verb)).collect(toList());
        this.rules_stop_if = prs.stream().filter(r -> "stop_if".equals(r.verb)).collect(toList());
        this.rules_stop_if_found = prs.stream().filter(r -> "stop_if_found".equals(r.verb)).collect(toList());
        this.rules_stop_unless = prs.stream().filter(r -> "stop_unless".equals(r.verb)).collect(toList());
        this.rules_stop_unless_found = prs.stream().filter(r -> "stop_unless_found".equals(r.verb)).collect(toList());
        this.skip_compression = prs.stream().anyMatch(r -> "skip_compression".equals(r.verb));
        this.skip_submission = prs.stream().anyMatch(r -> "skip_submission".equals(r.verb));

        // finish validating rules
        if (this.rules_sample.size() > 1) throw new IllegalArgumentException("Multiple sample rules");
        if ((url != null) && (url.startsWith("http:") && !rules_allow_http_url)) {
            this.enableable = false;
            this.enabled = false;
        }
    }

    private final Random random = new Random();
    private String rules;
    private boolean rules_allow_http_url;
    private List<HttpRule> rules_copy_session_field;
    private List<HttpRule> rules_remove;
    private List<HttpRule> rules_remove_if;
    private List<HttpRule> rules_remove_if_found;
    private List<HttpRule> rules_remove_unless;
    private List<HttpRule> rules_remove_unless_found;
    private List<HttpRule> rules_replace;
    private List<HttpRule> rules_sample;
    private List<HttpRule> rules_stop;
    private List<HttpRule> rules_stop_if;
    private List<HttpRule> rules_stop_if_found;
    private List<HttpRule> rules_stop_unless;
    private List<HttpRule> rules_stop_unless_found;

    /**
     * Returns rules specified when creating this logger.
     */
    public String getRules() {
        return rules;
    }

    /**
     * Logs HTTP request and response to intended destination.
     */
    public boolean log(HttpServletRequest request, HttpServletResponse response) {
        return log(request, response, null, null);
    }

    /**
     * Logs HTTP request and response to intended destination.
     */
    public boolean log(HttpServletRequest request, HttpServletResponse response, String response_body) {
        return log(request, response, response_body, null);
    }

    /**
     * Logs HTTP request and response to intended destination.
     */
    public boolean log(HttpServletRequest request, HttpServletResponse response, String response_body, String request_body) {
        return !isEnabled() || submit(format(request, response, response_body, request_body));
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
        List<String[]> details = HttpMessage.build(request, response, response_body, request_body);

        // copy data from session if configured
        if (!rules_copy_session_field.isEmpty()) {
            HttpSession ssn = request.getSession(false);
            if (ssn != null) {
                for (HttpRule r : rules_copy_session_field) {
                    Enumeration<String> names = ssn.getAttributeNames();
                    while (names.hasMoreElements()) {
                        String d = names.nextElement();
                        if (((Pattern) r.param1).matcher(d).matches()) {
                            String val = ssn.getAttribute(d).toString();
                            details.add(new String[]{"session_field:" + d, val});
                        }
                    }
                }
            }
        }

        // quit early based on stop rules if configured
        for (HttpRule r : rules_stop)
            for (String[] d : details)
                if (r.scope.matcher(d[0]).matches()) return null;
        for (HttpRule r : rules_stop_if_found)
            for (String[] d : details)
                if (r.scope.matcher(d[0]).matches() && ((Pattern) r.param1).matcher(d[1]).find()) return null;
        for (HttpRule r : rules_stop_if)
            for (String[] d : details)
                if (r.scope.matcher(d[0]).matches() && ((Pattern) r.param1).matcher(d[1]).matches()) return null;
        int passed = 0;
        for (HttpRule r : rules_stop_unless_found)
            for (String[] d : details)
                if (r.scope.matcher(d[0]).matches() && ((Pattern) r.param1).matcher(d[1]).find()) passed++;
        if (passed != rules_stop_unless_found.size()) return null;
        passed = 0;
        for (HttpRule r : rules_stop_unless)
            for (String[] d : details)
                if (r.scope.matcher(d[0]).matches() && ((Pattern) r.param1).matcher(d[1]).matches()) passed++;
        if (passed != rules_stop_unless.size()) return null;

        // do sampling if configured
        if ((rules_sample.size() == 1) && (random.nextInt(100) >= (Integer) rules_sample.get(0).param1)) return null;

        // winnow sensitive details based on remove rules if configured
        for (HttpRule r : rules_remove)
            details.removeIf(d -> r.scope.matcher(d[0]).matches());
        for (HttpRule r : rules_remove_unless_found)
            details.removeIf(d -> r.scope.matcher(d[0]).matches() && !((Pattern) r.param1).matcher(d[1]).find());
        for (HttpRule r : rules_remove_if_found)
            details.removeIf(d -> r.scope.matcher(d[0]).matches() && ((Pattern) r.param1).matcher(d[1]).find());
        for (HttpRule r : rules_remove_unless)
            details.removeIf(d -> r.scope.matcher(d[0]).matches() && !((Pattern) r.param1).matcher(d[1]).matches());
        for (HttpRule r : rules_remove_if)
            details.removeIf(d -> r.scope.matcher(d[0]).matches() && ((Pattern) r.param1).matcher(d[1]).matches());
        if (details.isEmpty()) return null;

        // mask sensitive details based on replace rules if configured
        for (HttpRule r : rules_replace)
            for (String[] d : details)
                if (r.scope.matcher(d[0]).matches()) d[1] = ((Pattern) r.param1).matcher(d[1]).replaceAll((String) r.param2);

        // remove any details with empty values
        details.removeIf(d -> "".equals(d[1]));
        if (details.isEmpty()) return null;

        // finish message
        details.add(new String[]{"now", String.valueOf(now)});
        details.add(new String[]{"agent", this.agent});
        details.add(new String[]{"version", this.version});
        return Json.stringify(details);
    }

}
