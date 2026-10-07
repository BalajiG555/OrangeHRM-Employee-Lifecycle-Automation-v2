package com.orangehrm.pages;

import com.framework.utils.WaitUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

/**
 * Admin &gt; User Management &gt; System Users - an admin-only screen used for role-based access checks.
 */
public class SystemUsersPage extends OrangeHrmPage {

    private static final By HEADER = By.xpath("//h5[normalize-space()='System Users']");

    public SystemUsersPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Navigates straight to the URL, bypassing the menu (the way a user would try to sidestep the UI).
     */
    public SystemUsersPage openDirectly() {
        openPath(AppRoutes.SYSTEM_USERS);
        WaitUtils.waitForPageReady();
        return this;
    }

    /**
     * {@code true} when the System Users screen actually rendered for the current user.
     */
    public boolean isAccessible() {
        try {
            waitForAppShell();
        } catch (TimeoutException e) {
            return false;
        }
        return isDisplayed(HEADER);
    }
}
