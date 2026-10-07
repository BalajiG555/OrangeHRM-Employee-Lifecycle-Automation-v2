package com.orangehrm.data;

import com.framework.config.ConfigKeys;
import com.framework.config.ConfigReader;
import com.orangehrm.models.Credentials;

/**
 * Supplies configured (secret) credentials. Values come from CI secrets or the git-ignored local
 * {@code config.properties}; nothing is hard-coded.
 */
public final class CredentialsProvider {

    private CredentialsProvider() {
    }

    public static Credentials admin() {
        return new Credentials(ConfigReader.getRequired(ConfigKeys.ADMIN_USERNAME),
                ConfigReader.getRequired(ConfigKeys.ADMIN_PASSWORD));
    }
}
