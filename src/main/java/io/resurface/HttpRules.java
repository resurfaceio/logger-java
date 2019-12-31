// © 2016-2019 Resurface Labs Inc.

package io.resurface;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.util.stream.Collectors.toList;

/**
 * Parser and utilities for HTTP logger rules.
 */
public class HttpRules {

    public static final String DEBUG_RULES = "allow_http_url\ncopy_session_field /.*/\n";

    public static final String STANDARD_RULES = "/request_header:cookie|response_header:set-cookie/remove\n" +
            "/(request|response)_body|request_param/ replace /[a-zA-Z0-9.!#$%&’*+\\/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)/, /x@y.com/\n" +
            "/request_body|request_param|response_body/ replace /[0-9\\.\\-\\/]{9,}/, /xyxy/\n";

    public static final String STRICT_RULES = "/request_url/ replace /([^\\?;]+).*/, /$1/\n" +
            "/request_body|response_body|request_param:.*|request_header:(?!user-agent).*|response_header:(?!(content-length)|(content-type)).*/ remove\n";

    private static String defaultRules = HttpRules.getStrictRules();

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

    /**
     * Rules providing all details for debugging an application.
     */
    public static String getDebugRules() {
        return DEBUG_RULES;
    }

    /**
     * Rules that block common kinds of sensitive data.
     */
    public static String getStandardRules() {
        return STANDARD_RULES;
    }

    /**
     * Rules providing minimal details, used by default.
     */
    public static String getStrictRules() {
        return STRICT_RULES;
    }

    /**
     * Parses rule from single line.
     */
    public static HttpRule parseRule(String r) {
        if ((r == null) || REGEX_BLANK_OR_COMMENT.matcher(r).matches()) return null;
        Matcher m = REGEX_ALLOW_HTTP_URL.matcher(r);
        if (m.matches()) return new HttpRule("allow_http_url", null, null, null);
        m = REGEX_COPY_SESSION_FIELD.matcher(r);
        if (m.matches()) return new HttpRule("copy_session_field", null, parseRegex(r, m.group(1)), null);
        m = REGEX_REMOVE.matcher(r);
        if (m.matches()) return new HttpRule("remove", parseRegex(r, m.group(1)), null, null);
        m = REGEX_REMOVE_IF.matcher(r);
        if (m.matches()) return new HttpRule("remove_if", parseRegex(r, m.group(1)), parseRegex(r, m.group(2)), null);
        m = REGEX_REMOVE_IF_FOUND.matcher(r);
        if (m.matches()) return new HttpRule("remove_if_found", parseRegex(r, m.group(1)), parseRegexFind(r, m.group(2)), null);
        m = REGEX_REMOVE_UNLESS.matcher(r);
        if (m.matches()) return new HttpRule("remove_unless", parseRegex(r, m.group(1)), parseRegex(r, m.group(2)), null);
        m = REGEX_REMOVE_UNLESS_FOUND.matcher(r);
        if (m.matches())
            return new HttpRule("remove_unless_found", parseRegex(r, m.group(1)), parseRegexFind(r, m.group(2)), null);
        m = REGEX_REPLACE.matcher(r);
        if (m.matches())
            return new HttpRule("replace", parseRegex(r, m.group(1)), parseRegexFind(r, m.group(2)), parseString(r, m.group(3)));
        m = REGEX_SAMPLE.matcher(r);
        if (m.matches()) {
            Integer m1 = Integer.valueOf(m.group(1));
            if (m1 < 1 || m1 > 99) throw new IllegalArgumentException(String.format("Invalid sample percent: %d", m1));
            return new HttpRule("sample", null, m1, null);
        }
        m = REGEX_SKIP_COMPRESSION.matcher(r);
        if (m.matches()) return new HttpRule("skip_compression", null, null, null);
        m = REGEX_SKIP_SUBMISSION.matcher(r);
        if (m.matches()) return new HttpRule("skip_submission", null, null, null);
        m = REGEX_STOP.matcher(r);
        if (m.matches()) return new HttpRule("stop", parseRegex(r, m.group(1)), null, null);
        m = REGEX_STOP_IF.matcher(r);
        if (m.matches()) return new HttpRule("stop_if", parseRegex(r, m.group(1)), parseRegex(r, m.group(2)), null);
        m = REGEX_STOP_IF_FOUND.matcher(r);
        if (m.matches()) return new HttpRule("stop_if_found", parseRegex(r, m.group(1)), parseRegexFind(r, m.group(2)), null);
        m = REGEX_STOP_UNLESS.matcher(r);
        if (m.matches()) return new HttpRule("stop_unless", parseRegex(r, m.group(1)), parseRegex(r, m.group(2)), null);
        m = REGEX_STOP_UNLESS_FOUND.matcher(r);
        if (m.matches()) return new HttpRule("stop_unless_found", parseRegex(r, m.group(1)), parseRegexFind(r, m.group(2)), null);
        throw new IllegalArgumentException(String.format("Invalid rule: %s", r));
    }

    /**
     * Parses regex for matching.
     */
    private static Pattern parseRegex(String r, String regex) {
        String s = parseString(r, regex);
        if ("*".equals(s) || "+".equals(s) || "?".equals(s))
            throw new IllegalArgumentException(String.format("Invalid regex (%s) in rule: %s", regex, r));
        if (!s.startsWith("^")) s = "^" + s;
        if (!s.endsWith("$")) s = s + "$";
        try {
            return Pattern.compile(s);
        } catch (PatternSyntaxException pse) {
            throw new IllegalArgumentException(String.format("Invalid regex (%s) in rule: %s", regex, r));
        }
    }

    /**
     * Parses regex for finding.
     */
    private static Pattern parseRegexFind(String r, String regex) {
        try {
            return Pattern.compile(parseString(r, regex));
        } catch (PatternSyntaxException pse) {
            throw new IllegalArgumentException(String.format("Invalid regex (%s) in rule: %s", regex, r));
        }
    }

    /**
     * Parses delimited string expression.
     */
    private static String parseString(String r, String expr) {
        for (String sep : new String[]{"~", "!", "%", "|", "/"}) {
            Matcher m = Pattern.compile(String.format("^[%s](.*)[%s]$", sep, sep)).matcher(expr);
            if (m.matches()) {
                String m1 = m.group(1);
                if (Pattern.compile(String.format("^[%s].*|.*[^\\\\][%s].*", sep, sep)).matcher(m1).matches())
                    throw new IllegalArgumentException(String.format("Unescaped separator (%s) in rule: %s", sep, r));
                return m1.replace("\\" + sep, sep);
            }
        }
        throw new IllegalArgumentException(String.format("Invalid expression (%s) in rule: %s", expr, r));
    }

    /**
     * Initialize a new set of rules.
     */
    public HttpRules(String rules) {
        if (rules == null) rules = HttpRules.getDefaultRules();

        // load rules from external files
        if (rules.startsWith("file://")) {
            String rfile = rules.substring(7).trim();
            try {
                rules = new String(Files.readAllBytes(Paths.get(rfile)));
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to load rules: " + rfile);
            }
        }

        // force default rules if necessary
        rules = rules.replaceAll("(?m)^\\s*include default\\s*$", Matcher.quoteReplacement(HttpRules.getDefaultRules()));
        if (rules.trim().length() == 0) rules = HttpRules.getDefaultRules();

        // expand rule includes
        rules = rules.replaceAll("(?m)^\\s*include debug\\s*$", Matcher.quoteReplacement(getDebugRules()));
        rules = rules.replaceAll("(?m)^\\s*include standard\\s*$", Matcher.quoteReplacement(getStandardRules()));
        rules = rules.replaceAll("(?m)^\\s*include strict\\s*$", Matcher.quoteReplacement(getStrictRules()));
        this.text = rules;

        // parse all rules
        List<HttpRule> prs = new ArrayList<>();
        for (String rule : this.text.split("\\r?\\n")) {
            HttpRule parsed = parseRule(rule);
            if (parsed != null) prs.add(parsed);
        }
        this.size = prs.size();

        // break out rules by verb
        this.allow_http_url = prs.stream().anyMatch(r -> "allow_http_url".equals(r.verb));
        this.copy_session_field = prs.stream().filter(r -> "copy_session_field".equals(r.verb)).collect(toList());
        this.remove = prs.stream().filter(r -> "remove".equals(r.verb)).collect(toList());
        this.remove_if = prs.stream().filter(r -> "remove_if".equals(r.verb)).collect(toList());
        this.remove_if_found = prs.stream().filter(r -> "remove_if_found".equals(r.verb)).collect(toList());
        this.remove_unless = prs.stream().filter(r -> "remove_unless".equals(r.verb)).collect(toList());
        this.remove_unless_found = prs.stream().filter(r -> "remove_unless_found".equals(r.verb)).collect(toList());
        this.replace = prs.stream().filter(r -> "replace".equals(r.verb)).collect(toList());
        this.sample = prs.stream().filter(r -> "sample".equals(r.verb)).collect(toList());
        this.skip_compression = prs.stream().anyMatch(r -> "skip_compression".equals(r.verb));
        this.skip_submission = prs.stream().anyMatch(r -> "skip_submission".equals(r.verb));
        this.stop = prs.stream().filter(r -> "stop".equals(r.verb)).collect(toList());
        this.stop_if = prs.stream().filter(r -> "stop_if".equals(r.verb)).collect(toList());
        this.stop_if_found = prs.stream().filter(r -> "stop_if_found".equals(r.verb)).collect(toList());
        this.stop_unless = prs.stream().filter(r -> "stop_unless".equals(r.verb)).collect(toList());
        this.stop_unless_found = prs.stream().filter(r -> "stop_unless_found".equals(r.verb)).collect(toList());

        // finish validating rules
        if (this.sample.size() > 1) throw new IllegalArgumentException("Multiple sample rules");
    }

    public final boolean allow_http_url;
    public final List<HttpRule> copy_session_field;
    public final List<HttpRule> remove;
    public final List<HttpRule> remove_if;
    public final List<HttpRule> remove_if_found;
    public final List<HttpRule> remove_unless;
    public final List<HttpRule> remove_unless_found;
    public final List<HttpRule> replace;
    public final List<HttpRule> sample;
    public final boolean skip_compression;
    public final boolean skip_submission;
    public final int size;
    public final List<HttpRule> stop;
    public final List<HttpRule> stop_if;
    public final List<HttpRule> stop_if_found;
    public final List<HttpRule> stop_unless;
    public final List<HttpRule> stop_unless_found;
    public final String text;

    /**
     * Apply current rules to message details.
     */
    public List<String[]> apply(List<String[]> details) {
        // stop rules come first
        for (HttpRule r : stop)
            for (String[] d : details)
                if (r.scope.matcher(d[0]).matches()) return null;
        for (HttpRule r : stop_if_found)
            for (String[] d : details)
                if (r.scope.matcher(d[0]).matches() && ((Pattern) r.param1).matcher(d[1]).find()) return null;
        for (HttpRule r : stop_if)
            for (String[] d : details)
                if (r.scope.matcher(d[0]).matches() && ((Pattern) r.param1).matcher(d[1]).matches()) return null;
        int passed = 0;
        for (HttpRule r : stop_unless_found)
            for (String[] d : details)
                if (r.scope.matcher(d[0]).matches() && ((Pattern) r.param1).matcher(d[1]).find()) passed++;
        if (passed != stop_unless_found.size()) return null;
        passed = 0;
        for (HttpRule r : stop_unless)
            for (String[] d : details)
                if (r.scope.matcher(d[0]).matches() && ((Pattern) r.param1).matcher(d[1]).matches()) passed++;
        if (passed != stop_unless.size()) return null;

        // do sampling if configured
        if ((sample.size() == 1) && (RANDOM.nextInt(100) >= (Integer) sample.get(0).param1)) return null;

        // winnow sensitive details based on remove rules if configured
        for (HttpRule r : remove)
            details.removeIf(d -> r.scope.matcher(d[0]).matches());
        for (HttpRule r : remove_unless_found)
            details.removeIf(d -> r.scope.matcher(d[0]).matches() && !((Pattern) r.param1).matcher(d[1]).find());
        for (HttpRule r : remove_if_found)
            details.removeIf(d -> r.scope.matcher(d[0]).matches() && ((Pattern) r.param1).matcher(d[1]).find());
        for (HttpRule r : remove_unless)
            details.removeIf(d -> r.scope.matcher(d[0]).matches() && !((Pattern) r.param1).matcher(d[1]).matches());
        for (HttpRule r : remove_if)
            details.removeIf(d -> r.scope.matcher(d[0]).matches() && ((Pattern) r.param1).matcher(d[1]).matches());
        if (details.isEmpty()) return null;

        // mask sensitive details based on replace rules if configured
        for (HttpRule r : replace)
            for (String[] d : details)
                if (r.scope.matcher(d[0]).matches()) d[1] = ((Pattern) r.param1).matcher(d[1]).replaceAll((String) r.param2);

        // remove any details with empty values
        details.removeIf(d -> "".equals(d[1]));
        if (details.isEmpty()) return null;

        return details;
    }

    private static final Random RANDOM = new Random();
    private static final Pattern REGEX_ALLOW_HTTP_URL = Pattern.compile("^\\s*allow_http_url\\s*(#.*)?$");
    private static final Pattern REGEX_BLANK_OR_COMMENT = Pattern.compile("^\\s*([#].*)*$");
    private static final Pattern REGEX_COPY_SESSION_FIELD = Pattern.compile("^\\s*copy_session_field\\s+([~!%|/].+[~!%|/])\\s*(#.*)?$");
    private static final Pattern REGEX_REMOVE = Pattern.compile("^\\s*([~!%|/].+[~!%|/])\\s*remove\\s*(#.*)?$");
    private static final Pattern REGEX_REMOVE_IF = Pattern.compile("^\\s*([~!%|/].+[~!%|/])\\s*remove_if\\s+([~!%|/].+[~!%|/])\\s*(#.*)?$");
    private static final Pattern REGEX_REMOVE_IF_FOUND = Pattern.compile("^\\s*([~!%|/].+[~!%|/])\\s*remove_if_found\\s+([~!%|/].+[~!%|/])\\s*(#.*)?$");
    private static final Pattern REGEX_REMOVE_UNLESS = Pattern.compile("^\\s*([~!%|/].+[~!%|/])\\s*remove_unless\\s+([~!%|/].+[~!%|/])\\s*(#.*)?$");
    private static final Pattern REGEX_REMOVE_UNLESS_FOUND = Pattern.compile("^\\s*([~!%|/].+[~!%|/])\\s*remove_unless_found\\s+([~!%|/].+[~!%|/])\\s*(#.*)?$");
    private static final Pattern REGEX_REPLACE = Pattern.compile("^\\s*([~!%|/].+[~!%|/])\\s*replace[\\s]+([~!%|/].+[~!%|/]),[\\s]+([~!%|/].*[~!%|/])\\s*(#.*)?$");
    private static final Pattern REGEX_SAMPLE = Pattern.compile("^\\s*sample\\s+(\\d+)\\s*(#.*)?$");
    private static final Pattern REGEX_SKIP_COMPRESSION = Pattern.compile("^\\s*skip_compression\\s*(#.*)?$");
    private static final Pattern REGEX_SKIP_SUBMISSION = Pattern.compile("^\\s*skip_submission\\s*(#.*)?$");
    private static final Pattern REGEX_STOP = Pattern.compile("^\\s*([~!%|/].+[~!%|/])\\s*stop\\s*(#.*)?$");
    private static final Pattern REGEX_STOP_IF = Pattern.compile("^\\s*([~!%|/].+[~!%|/])\\s*stop_if\\s+([~!%|/].+[~!%|/])\\s*(#.*)?$");
    private static final Pattern REGEX_STOP_IF_FOUND = Pattern.compile("^\\s*([~!%|/].+[~!%|/])\\s*stop_if_found\\s+([~!%|/].+[~!%|/])\\s*(#.*)?$");
    private static final Pattern REGEX_STOP_UNLESS = Pattern.compile("^\\s*([~!%|/].+[~!%|/])\\s*stop_unless\\s+([~!%|/].+[~!%|/])\\s*(#.*)?$");
    private static final Pattern REGEX_STOP_UNLESS_FOUND = Pattern.compile("^\\s*([~!%|/].+[~!%|/])\\s*stop_unless_found\\s+([~!%|/].+[~!%|/])\\s*(#.*)?$");

}
