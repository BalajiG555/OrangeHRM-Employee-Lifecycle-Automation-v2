package com.framework.utils;

/**
 * Helpers for building XPath expressions safely from dynamic text.
 */
public final class XPathUtils {

    private XPathUtils() {
    }

    /**
     * Returns {@code value} as an XPath string literal, correctly handling embedded quotes
     * (e.g. an employee called O'Brien would otherwise break a hand-built locator).
     */
    public static String literal(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        return "concat('" + value.replace("'", "', \"'\", '") + "')";
    }
}
