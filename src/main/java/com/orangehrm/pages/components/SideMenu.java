package com.orangehrm.pages.components;

import com.framework.pages.BasePage;
import com.framework.utils.WaitUtils;
import com.framework.utils.XPathUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * The left-hand main menu - a component object reused by every page. Its content depends on the
 * logged-in user's role, which makes it the primary role-based-access check.
 */
public class SideMenu extends BasePage {

    private static final By MENU = By.cssSelector("ul.oxd-main-menu");

    public SideMenu(WebDriver driver) {
        super(driver);
    }

    /**
     * Waits for the menu to render, then checks the item without further waiting, so a "hidden" assertion is
     * both fast and meaningful (the menu has demonstrably loaded).
     */
    public boolean isItemVisible(String itemName) {
        WaitUtils.waitFor(WaitUtils.navigationTimeout()).until(ExpectedConditions.visibilityOfElementLocated(MENU));
        return isPresentNow(menuItem(itemName));
    }

    public void open(String itemName) {
        click(menuItem(itemName));
    }

    private static By menuItem(String itemName) {
        return By.xpath("//a[contains(@class,'oxd-main-menu-item')][.//span[normalize-space()="
                + XPathUtils.literal(itemName) + "]]");
    }
}
