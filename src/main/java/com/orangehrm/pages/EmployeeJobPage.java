package com.orangehrm.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Employee profile &gt; Job.
 */
public class EmployeeJobPage extends OrangeHrmPage {

    private static final By HEADER = By.xpath("//h6[normalize-space()='Job Details']");
    private static final By JOB_TITLE = OxdLocators.dropdownByLabel("Job Title");
    private static final By EMPLOYMENT_STATUS = OxdLocators.dropdownByLabel("Employment Status");
    private static final By SAVE_BUTTON = OxdLocators.buttonByText("Save");

    public EmployeeJobPage(WebDriver driver) {
        super(driver);
    }

    public EmployeeJobPage waitUntilLoaded() {
        waitForServer(HEADER);
        waitForLoaderToDisappear();
        return this;
    }

    public EmployeeJobPage updateJobDetails(String jobTitle, String employmentStatus) {
        selectDropdownOption(JOB_TITLE, jobTitle);
        selectDropdownOption(EMPLOYMENT_STATUS, employmentStatus);
        clickUnobstructed(SAVE_BUTTON);
        waitForSuccessToast();
        waitForLoaderToDisappear();
        return this;
    }

    public String getJobTitle() {
        waitForLoaderToDisappear();
        return getText(JOB_TITLE);
    }

    public String getEmploymentStatus() {
        waitForLoaderToDisappear();
        return getText(EMPLOYMENT_STATUS);
    }
}
