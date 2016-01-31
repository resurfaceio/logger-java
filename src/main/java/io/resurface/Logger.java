// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import java.io.InputStream;
import java.util.Properties;

/**
 * Java library for usage logging.
 */
public class Logger {
    /**
     * Returns version number from generated properties file.
     */
    public String getVersion() {
        try (InputStream is = getClass().getResourceAsStream("/version.properties")) {
            Properties p = new Properties();
            p.load(is);
            return p.getProperty("version", null);
        } catch (Exception e) {
            throw new RuntimeException("Version could not be loaded: " + e.getMessage());
        }
    }
}
