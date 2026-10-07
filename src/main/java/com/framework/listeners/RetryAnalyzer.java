package com.framework.listeners;

import com.framework.config.ConfigKeys;
import com.framework.config.ConfigReader;
import com.framework.exceptions.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failed scenario up to {@code retry.max.count} times (configurable per environment).
 *
 * <p>Retries are a mitigation, not a cure: every retry is logged and reported by {@link FlakyTestListener}
 * so flaky tests stay visible. Configuration errors are never retried because they cannot self-heal.</p>
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger LOG = LoggerFactory.getLogger(RetryAnalyzer.class);

    private final int maxRetries = Math.max(0, ConfigReader.getInt(ConfigKeys.RETRY_MAX_COUNT, 1));
    private int attempts;

    @Override
    public boolean retry(ITestResult result) {
        if (isNonRetriable(result.getThrowable())) {
            LOG.error("Not retrying '{}': failure is a configuration error.", TestNames.of(result));
            return false;
        }
        if (attempts < maxRetries) {
            attempts++;
            LOG.warn("Retrying '{}' (retry {}/{}) after: {}", TestNames.of(result), attempts, maxRetries,
                    result.getThrowable() == null ? "unknown error" : result.getThrowable().getMessage());
            return true;
        }
        return false;
    }

    private static boolean isNonRetriable(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ConfigurationException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
