package com.orangehrm.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

/**
 * Landing page after login, plus the user menu (logout).
 */
public class DashboardPage extends OrangeHrmPage {

    private static final By HEADER = By.xpath("//h6[normalize-space()='Dashboard']");
    private static final By USER_MENU = By.cssSelector(".oxd-userdropdown-tab");
    private static final By LOGOUT = By.xpath("//a[normalize-space()='Logout']");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Waits for the app shell (navigation timeout), then for the Dashboard header (element timeout).
     */
    public boolean isLoaded() {
        try {
            waitForAppShell();
        } catch (TimeoutException e) {
            return false;
        }
        return isDisplayed(HEADER) && currentUrl().contains(AppRoutes.DASHBOARD);
    }

    public LoginPage logout() {
        click(USER_MENU);
        click(LOGOUT);
        return new LoginPage(driver);
    }
}
