package com.framework.unit;

import com.framework.config.ConfigKeys;
import com.framework.config.ConfigReader;
import com.framework.exceptions.ConfigurationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Configuration layering: environment file over defaults, system property over everything, fail-fast errors.
 */
public class ConfigReaderTest {

    @Test
    public void environmentFileOverridesDefaults() {
        // config-default.properties: headless=true ; config-local.properties: headless=false
        Assert.assertEquals(ConfigReader.getEnvironment(), "local");
        Assert.assertFalse(ConfigReader.getBoolean(ConfigKeys.HEADLESS, true));
        Assert.assertEquals(ConfigReader.get(ConfigKeys.BROWSER), "chrome", "inherited from defaults");
    }

    @Test
    public void systemPropertyHasHighestPriority() {
        String key = ConfigKeys.EXPLICIT_WAIT_SECONDS;
        try {
            System.setProperty(key, "42");
            Assert.assertEquals(ConfigReader.getInt(key, 0), 42);
        } finally {
            System.clearProperty(key);
        }
        Assert.assertEquals(ConfigReader.getInt(key, 0), 15);
    }

    @Test(expectedExceptions = ConfigurationException.class,
            expectedExceptionsMessageRegExp = ".*does.not.exist.*ORANGEHRM_DOES_NOT_EXIST.*")
    public void missingRequiredValueFailsFastWithHint() {
        ConfigReader.getRequired("does.not.exist");
    }

    @Test(expectedExceptions = ConfigurationException.class)
    public void nonNumericIntegerIsRejected() {
        try {
            System.setProperty("unit.number", "abc");
            ConfigReader.getInt("unit.number", 1);
        } finally {
            System.clearProperty("unit.number");
        }
    }
}
