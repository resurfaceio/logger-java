// © 2016-2023 Resurface Labs Inc.

package io.resurface;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock HttpSession implementation.
 */
public class HttpSessionImpl extends BaseSessionImpl {

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    private final Map<String, Object> attributes = new HashMap<>();
}
