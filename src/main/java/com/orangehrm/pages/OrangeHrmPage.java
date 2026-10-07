package com.orangehrm.pages;

import com.framework.pages.BasePage;
import com.framework.utils.WaitUtils;
import com.orangehrm.pages.components.SideMenu;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Base for OrangeHRM pages: adds the app-specific behaviours shared by every screen (loader overlays,
 * toasts, dropdowns, side menu) on top of the generic {@link BasePage}.
 */
public abstract class OrangeHrmPage extends BasePage {

    protected static final By FORM_LOADER = By.cssSelector(".oxd-form-loader");
    protected static final By SUCCESS_TOAST = By.cssSelector(".oxd-toast--success");
    /** Rendered on every authenticated page once the OrangeHRM single-page app has mounted. */
    protected static final By APP_SHELL = By.cssSelector("ul.oxd-main-menu");

    protected OrangeHrmPage(WebDriver driver) {
        super(driver);
    }

    public SideMenu sideMenu() {
        return new SideMenu(driver);
    }

    /**
     * Waits - up to the navigation timeout - for the authenticated application shell to render after a
     * server round-trip. Before it renders the page is blank, so any element check would be meaningless.
     */
    protected void waitForAppShell() {
        waitForServer(APP_SHELL);
    }

    /**
     * Waits for an element whose appearance depends on a server response (navigation, save, search, data
     * load), bounded by the navigation timeout. Use {@code waitForVisible} only for elements on a page
     * that has already loaded.
     */
    protected WebElement waitForServer(By locator) {
        return WaitUtils.navigationWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void waitForLoaderToDisappear() {
        WaitUtils.waitForInvisibility(FORM_LOADER);
    }

    /**
     * Clicks once no form loader overlays the element.
     */
    protected void clickUnobstructed(By locator) {
        WaitUtils.clickWhenUnobstructed(locator, FORM_LOADER);
    }

    protected void selectDropdownOption(By dropdown, String optionText) {
        clickUnobstructed(dropdown);
        click(OxdLocators.dropdownOption(optionText));
        waitForLoaderToDisappear();
    }

    /**
     * The success toast appears only after the server has saved the change.
     */
    protected void waitForSuccessToast() {
        waitForServer(SUCCESS_TOAST);
    }
}
