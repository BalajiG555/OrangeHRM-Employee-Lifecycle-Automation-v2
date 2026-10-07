package com.orangehrm.pages;

import com.framework.exceptions.FrameworkException;
import com.framework.utils.WaitUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.regex.Matcher;

/**
 * Employee profile &gt; Personal Details.
 */
public class EmployeeDetailsPage extends OrangeHrmPage {

    private static final By HEADER = By.xpath("//h6[normalize-space()='Personal Details']");
    private static final By FIRST_NAME = By.name("firstName");
    private static final By LAST_NAME = By.name("lastName");
    private static final By JOB_TAB = By.xpath("//a[normalize-space()='Job']");

    public EmployeeDetailsPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Waits for the personal-details route, header and asynchronously loaded form values.
     */
    public EmployeeDetailsPage waitUntilLoaded() {
        WaitUtils.navigationWait().until(d -> AppRoutes.PERSONAL_DETAILS.matcher(d.getCurrentUrl()).find());
        waitForServer(HEADER);
        waitForLoaderToDisappear();
        WaitUtils.navigationWait().until(d -> !getValue(FIRST_NAME).isEmpty());
        return this;
    }

    public boolean isLoaded() {
        return isDisplayed(HEADER) && AppRoutes.PERSONAL_DETAILS.matcher(currentUrl()).find();
    }

    public String getFirstName() {
        return getValue(FIRST_NAME);
    }

    public String getLastName() {
        return getValue(LAST_NAME);
    }

    /**
     * Reads the backend {@code empNumber} from the URL.
     */
    public int getEmpNumber() {
        Matcher matcher = AppRoutes.PERSONAL_DETAILS.matcher(currentUrl());
        if (!matcher.find()) {
            throw new FrameworkException("Not on a Personal Details page: " + currentUrl());
        }
        return Integer.parseInt(matcher.group(1));
    }

    public EmployeeJobPage openJobTab() {
        clickUnobstructed(JOB_TAB);
        EmployeeJobPage jobPage = new EmployeeJobPage(driver);
        jobPage.waitUntilLoaded();
        return jobPage;
    }
}
