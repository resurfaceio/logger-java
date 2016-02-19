// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

/**
 * Utility methods for JSON messages.
 */
public class Message {

    /**
     * Adds name/value pair to JSON message.
     */
    public static StringBuilder append(StringBuilder json, CharSequence key, CharSequence value) {
        json.append("\"").append(key.toString()).append("\":\"");
        escape(json, value);
        json.append("\"");
        return json;
    }

    /**
     * Adds name/value pair to JSON message.
     */
    public static StringBuilder append(StringBuilder json, CharSequence key, Integer value) {
        return json.append("\"").append(key.toString()).append("\":").append(value);
    }

    /**
     * Starts JSON message using given params.
     */
    public static StringBuilder define(StringBuilder json, CharSequence type, CharSequence version, long now) {
        json.append("{\"type\":\"")
                .append(type)
                .append("\",\"source\":\"resurfaceio-logger-java\",\"version\":\"")
                .append(version)
                .append("\",\"now\":")
                .append(now);
        return json;
    }

    /**
     * Escapes quotes and control characters in string value for use in JSON message.
     * Adapted from https://code.google.com/archive/p/json-simple (JSONValue.java)
     * This version has several changes from the original:
     * 1) Uses StringBuilder in place of StringBuffer
     * 2) Takes CharSequence so either String or StringBuilder can be supplied
     * 3) Skips escaping forward slashes
     * 4) Returns StringBuilder rather than void
     */
    public static StringBuilder escape(StringBuilder json, CharSequence value) {
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
                        String ss = Integer.toHexString(ch);
                        json.append("\\u");
                        for (int k = 0; k < 4 - ss.length(); k++) {
                            json.append('0');
                        }
                        json.append(ss.toUpperCase());
                    } else {
                        json.append(ch);
                    }
            }
        }
        return json;
    }

    /**
     * Finishes JSON message.
     */
    public static StringBuilder finish(StringBuilder json) {
        return json.append('}');
    }
}
