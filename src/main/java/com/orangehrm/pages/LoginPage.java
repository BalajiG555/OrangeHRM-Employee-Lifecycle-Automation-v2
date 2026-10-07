package com.orangehrm.pages;

import com.framework.exceptions.FrameworkException;
import com.framework.utils.WaitUtils;
import com.orangehrm.models.Credentials;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OrangeHRM login screen.
 */
public class LoginPage extends OrangeHrmPage {

    private static final Logger LOG = LoggerFactory.getLogger(LoginPage.class);
    private static final long NANOS_PER_MILLI = 1_000_000L;

    private static final By USERNAME = By.name("username");
    private static final By PASSWORD = By.name("password");
    private static final By LOGIN_BUTTON = By.cssSelector("button[type='submit']");
    private static final By ERROR_MESSAGE = By.cssSelector(".oxd-alert-content-text");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        openPath(AppRoutes.LOGIN);
        waitForServer(USERNAME);
        return this;
    }

    /**
     * Waits for the login route and form; returns {@code true} when the login page is shown.
     */
    public boolean isLoaded() {
        try {
            WaitUtils.navigationWait().until(ExpectedConditions.urlContains(AppRoutes.LOGIN));
            waitForServer(LOGIN_BUTTON);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Logs in and waits for the server's answer: either the browser leaves the login page, or the login
     * is rejected. A rejection fails immediately with the application's message, so "bad credentials" can
     * never be confused with "slow server".
     */
    public DashboardPage loginAs(Credentials credentials) {
        long start = System.nanoTime();
        submit(credentials);
        try {
            WaitUtils.waitFor(WaitUtils.navigationTimeout()).until(d ->
                    !d.getCurrentUrl().contains(AppRoutes.LOGIN) || !d.findElements(ERROR_MESSAGE).isEmpty());
        } catch (TimeoutException e) {
            throw new FrameworkException("Login for '" + credentials.username() + "' got no response within "
                    + WaitUtils.navigationTimeout().toSeconds() + " s - the application is responding slowly.", e);
        }
        if (currentUrl().contains(AppRoutes.LOGIN)) {
            throw new FrameworkException("Login was rejected for '" + credentials.username() + "': "
                    + getErrorMessage());
        }
        LOG.info("Logged in as '{}' in {} ms", credentials.username(), (System.nanoTime() - start) / NANOS_PER_MILLI);
        return new DashboardPage(driver);
    }

    /**
     * Submits credentials expected to be rejected and stays on this page.
     */
    public LoginPage attemptLogin(Credentials credentials) {
        submit(credentials);
        return this;
    }

    public String getErrorMessage() {
        return getText(ERROR_MESSAGE);
    }

    private void submit(Credentials credentials) {
        type(USERNAME, credentials.username());
        type(PASSWORD, credentials.password());
        click(LOGIN_BUTTON);
    }
}
