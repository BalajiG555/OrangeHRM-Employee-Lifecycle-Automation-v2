package com.framework.reporting.video;

import com.framework.exceptions.ConfigurationException;

import java.util.Locale;

/**
 * When scenario videos are recorded and kept.
 */
public enum VideoMode {
    /** Never record. */
    OFF,
    /** Record every scenario, keep the video only if it fails (default - cheap and useful). */
    ON_FAILURE,
    /** Record and keep every scenario. */
    ALWAYS;

    /**
     * Parses values such as {@code on-failure}, {@code ALWAYS}, {@code off}.
     */
    public static VideoMode from(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException(
                    "video.mode must be off, on-failure or always but was '" + value + "'.", e);
        }
    }

    public boolean isEnabled() {
        return this != OFF;
    }

    public boolean shouldKeep(boolean scenarioFailed) {
        return this == ALWAYS || (this == ON_FAILURE && scenarioFailed);
    }
}
