package com.orangehrm.pages;

import com.framework.utils.XPathUtils;

import org.openqa.selenium.By;

/**
 * Locator builders for OrangeHRM's "oxd" component library, which renders fields as label + input groups
 * without stable ids. Centralising them keeps every page free of duplicated XPath.
 */
public final class OxdLocators {

    private OxdLocators() {
    }

    public static By inputByLabel(String label) {
        return By.xpath("//label[normalize-space()=" + XPathUtils.literal(label) + "]"
                + "/ancestor::div[contains(@class,'oxd-input-group')]//input");
    }

    public static By dropdownByLabel(String label) {
        return By.xpath("//label[normalize-space()=" + XPathUtils.literal(label) + "]"
                + "/ancestor::div[contains(@class,'oxd-input-group')]//div[contains(@class,'oxd-select-text')]");
    }

    public static By dropdownOption(String optionText) {
        return By.xpath("//div[@role='option']//span[normalize-space()=" + XPathUtils.literal(optionText) + "]");
    }

    public static By buttonByText(String text) {
        return By.xpath("//button[normalize-space()=" + XPathUtils.literal(text) + "]");
    }
}
