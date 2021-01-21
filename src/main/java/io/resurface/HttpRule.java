// © 2016-2021 Resurface Labs Inc.

package io.resurface;

import java.util.regex.Pattern;

/**
 * Parsed rule for HTTP logger.
 */
public class HttpRule {

    public HttpRule(String verb, Pattern scope, Object param1, Object param2) {
        this.verb = verb;
        this.scope = scope;
        this.param1 = param1;
        this.param2 = param2;
    }

    public final String verb;
    public final Pattern scope;
    public final Object param1;
    public final Object param2;

}
