package com.orangehrm.api.auth;

import com.framework.api.AuthStrategy;
import com.framework.config.ConfigKeys;
import com.framework.config.ConfigReader;
import com.framework.exceptions.ConfigurationException;
import com.orangehrm.data.CredentialsProvider;

import java.util.Locale;

/**
 * Creates the {@link AuthStrategy} selected by {@code api.auth.mode} ({@code form} | {@code session} | {@code oauth}).
 */
public final class AuthStrategyFactory {

    private AuthStrategyFactory() {
    }

    /**
     * Builds the configured strategy for the given API base URI.
     */
    public static AuthStrategy fromConfig(String apiBaseUri) {
        String mode = ConfigReader.get(ConfigKeys.API_AUTH_MODE, "form").toLowerCase(Locale.ROOT);
        return switch (mode) {
            case "form" -> new FormLoginAuthStrategy(apiBaseUri, CredentialsProvider.admin());
            case "session" -> new SessionCookieAuthStrategy();
            case "oauth" -> new OAuthAuthStrategy(apiBaseUri);
            default -> throw new ConfigurationException(
                    "Unsupported api.auth.mode '" + mode + "'. Use form, session or oauth.");
        };
    }
}
