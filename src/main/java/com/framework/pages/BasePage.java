package com.framework.pages;

import com.framework.config.ConfigKeys;
import com.framework.config.ConfigReader;
import com.framework.utils.WaitUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.Objects;

/**
 * Application-agnostic base for all page and component objects. It wraps raw WebDriver calls in
 * explicit waits so page classes only describe <em>what</em> the page does, never <em>how</em> to wait.
 */
public abstract class BasePage {

    protected final WebDriver driver;

    protected BasePage(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver, "driver must not be null");
    }

    protected WebElement waitForVisible(By locator) {
        return WaitUtils.waitForVisible(locator);
    }

    protected void click(By locator) {
        WaitUtils.waitForClickable(locator).click();
    }

    /**
     * Clears the field (robust against JS-controlled inputs where {@code clear()} is ignored) and types.
     */
    protected void type(By locator, String text) {
        WebElement element = waitForVisible(locator);
        element.sendKeys(Keys.chord(selectAllModifier(), "a"), Keys.DELETE);
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return waitForVisible(locator).getText().trim();
    }

    protected String getValue(By locator) {
        String value = waitForVisible(locator).getDomProperty("value");
        return value == null ? "" : value.trim();
    }

    /**
     * Positive visibility check using the default explicit wait.
     */
    protected boolean isDisplayed(By locator) {
        return WaitUtils.isVisibleWithin(locator, WaitUtils.defaultTimeout());
    }

    /**
     * Visibility check with a caller-defined timeout.
     */
    protected boolean isDisplayedWithin(By locator, Duration timeout) {
        return WaitUtils.isVisibleWithin(locator, timeout);
    }

    /**
     * Immediate (non-waiting) presence check. Use only once the surrounding container has rendered,
     * so that "element is absent" assertions are fast but not vacuous.
     */
    protected boolean isPresentNow(By locator) {
        try {
            return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    /**
     * Navigates to a path relative to {@code app.base.url}.
     */
    protected void openPath(String relativePath) {
        driver.get(ConfigReader.getRequired(ConfigKeys.APP_BASE_URL) + relativePath);
    }

    protected String currentUrl() {
        return driver.getCurrentUrl();
    }

    private static Keys selectAllModifier() {
        return Platform.getCurrent().is(Platform.MAC) ? Keys.COMMAND : Keys.CONTROL;
    }
}
