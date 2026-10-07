package com.framework.utils;

import com.framework.config.ConfigKeys;
import com.framework.config.ConfigReader;
import com.framework.driver.DriverFactory;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Dynamic synchronisation helpers. The framework never uses {@code Thread.sleep}: every wait polls
 * for a concrete condition and returns as soon as it is met (enforced by a Checkstyle rule).
 */
public final class WaitUtils {

    private static final int DEFAULT_EXPLICIT_WAIT_SECONDS = 15;
    private static final int DEFAULT_SHORT_WAIT_SECONDS = 3;
    private static final int DEFAULT_NAVIGATION_WAIT_SECONDS = 30;

    private WaitUtils() {
    }

    /**
     * Standard wait using {@code explicit.wait.seconds}; ignores stale-element races.
     */
    public static WebDriverWait defaultWait() {
        return waitFor(defaultTimeout());
    }

    /**
     * Wait with a custom timeout; ignores stale-element races.
     */
    public static WebDriverWait waitFor(Duration timeout) {
        WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), timeout);
        wait.ignoring(StaleElementReferenceException.class);
        return wait;
    }

    /**
     * Timeout configured by {@code explicit.wait.seconds}.
     */
    public static Duration defaultTimeout() {
        return Duration.ofSeconds(ConfigReader.getInt(ConfigKeys.EXPLICIT_WAIT_SECONDS, DEFAULT_EXPLICIT_WAIT_SECONDS));
    }

    /**
     * Timeout configured by {@code short.wait.seconds}, used for "should NOT appear" checks.
     */
    public static Duration shortTimeout() {
        return Duration.ofSeconds(ConfigReader.getInt(ConfigKeys.SHORT_WAIT_SECONDS, DEFAULT_SHORT_WAIT_SECONDS));
    }

    /**
     * Timeout for waits that span a server round-trip (login, redirects, full page loads), configured by
     * {@code page.load.timeout.seconds}. Kept separate from the in-page element wait because a slow
     * server response is a different condition from a slow-rendering element.
     */
    public static Duration navigationTimeout() {
        return Duration.ofSeconds(
                ConfigReader.getInt(ConfigKeys.PAGE_LOAD_TIMEOUT_SECONDS, DEFAULT_NAVIGATION_WAIT_SECONDS));
    }

    /**
     * Wait bounded by {@link #navigationTimeout()}, for conditions that depend on a server response.
     */
    public static WebDriverWait navigationWait() {
        return waitFor(navigationTimeout());
    }

    public static WebElement waitForVisible(By locator) {
        return defaultWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForClickable(By locator) {
        return defaultWait().until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static void waitForInvisibility(By locator) {
        defaultWait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public static void waitForUrlContains(String fragment) {
        defaultWait().until(ExpectedConditions.urlContains(fragment));
    }

    /**
     * Returns {@code true} if the element becomes visible within {@code timeout}; never throws on timeout.
     */
    public static boolean isVisibleWithin(By locator, Duration timeout) {
        try {
            waitFor(timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Waits until the browser reports {@code document.readyState == 'complete'}.
     */
    public static void waitForPageReady() {
        defaultWait().until(driver ->
                "complete".equals(((JavascriptExecutor) driver).executeScript("return document.readyState")));
    }

    /**
     * Clicks an element as soon as no overlay (e.g. a loading spinner) covers it.
     *
     * <p>OrangeHRM re-renders its form loader asynchronously, so a plain "wait for invisibility then click"
     * still races. This retries the whole check-and-click atomically until it succeeds.</p>
     */
    public static void clickWhenUnobstructed(By elementLocator, By overlayLocator) {
        WebDriverWait wait = defaultWait();
        wait.ignoreAll(List.of(
                NoSuchElementException.class,
                ElementClickInterceptedException.class,
                StaleElementReferenceException.class));
        wait.until(driver -> {
            boolean overlayVisible = driver.findElements(overlayLocator).stream().anyMatch(WebElement::isDisplayed);
            if (overlayVisible) {
                return false;
            }
            WebElement element = driver.findElement(elementLocator);
            if (!element.isDisplayed() || !element.isEnabled()) {
                return false;
            }
            element.click();
            return true;
        });
    }
}
