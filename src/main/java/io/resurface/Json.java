// © 2016-2022 Resurface Labs Inc.

package io.resurface;

import java.util.List;

/**
 * Utility methods for formatting JSON messages.
 */
public class Json {

    /**
     * Adds comma-delimited key/value pair to message.
     */
    public static StringBuilder append(StringBuilder json, CharSequence key, CharSequence value) {
        if ((key != null) && (value != null)) {
            json.append("\"").append(key.toString()).append("\",\"");
            escape(json, value).append("\"");
        }
        return json;
    }

    /**
     * Escapes quotes and control characters in string value for use in message.
     * Adapted from https://code.google.com/archive/p/json-simple (JSONValue.java)
     * This version has several changes from the original:
     * 1) Uses StringBuilder in place of StringBuffer
     * 2) Takes CharSequence so either String or StringBuilder can be supplied
     * 3) Skips escaping forward slashes
     * 4) Returns StringBuilder rather than void
     */
    public static StringBuilder escape(StringBuilder json, CharSequence value) {
        if (value == null) return json;
        final int len = value.length();
        for (int i = 0; i < len; i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"':
                    json.append("\\\"");
                    break;
                case '\\':
                    json.append("\\\\");
                    break;
                case '\b':
                    json.append("\\b");
                    break;
                case '\f':
                    json.append("\\f");
                    break;
                case '\n':
                    json.append("\\n");
                    break;
                case '\r':
                    json.append("\\r");
                    break;
                case '\t':
                    json.append("\\t");
                    break;
                default:
                    if ((ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
                        json.append("\\u");
                        String ss = Integer.toHexString(ch);
                        for (int k = 0; k < 4 - ss.length(); k++) json.append('0');
                        json.append(ss.toUpperCase());
                    } else {
                        json.append(ch);
                    }
            }
        }
        return json;
    }

    /**
     * Formats list of string arrays as JSON.
     */
    public static String stringify(List<String[]> message) {
        StringBuilder json = new StringBuilder(1024);
        json.append('[');
        int idx = 0;
        for (String[] entry : message) {
            json.append(idx++ > 0 ? ",[" : '[');
            append(json, entry[0], entry[1]).append(']');
        }
        json.append(']');
        return json.toString();
    }

}
