package com.orangehrm.pages;

import com.framework.utils.WaitUtils;
import com.framework.utils.XPathUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * PIM &gt; Employee List: search, open and delete employees.
 */
public class EmployeeListPage extends OrangeHrmPage {

    private static final String PIM_MENU = "PIM";
    private static final By EMPLOYEE_ID_FILTER = OxdLocators.inputByLabel("Employee Id");
    private static final By SEARCH_BUTTON = OxdLocators.buttonByText("Search");
    private static final By CONFIRM_DELETE = OxdLocators.buttonByText("Yes, Delete");
    private static final By NO_RECORDS = By.xpath("//span[normalize-space()='No Records Found']");

    public EmployeeListPage(WebDriver driver) {
        super(driver);
    }

    public EmployeeListPage open() {
        sideMenu().open(PIM_MENU);
        waitForServer(EMPLOYEE_ID_FILTER);
        return this;
    }

    /**
     * Filters by Employee Id and waits until the table settles on either the matching row or "No Records".
     */
    public EmployeeListPage searchByEmployeeId(String employeeId) {
        type(EMPLOYEE_ID_FILTER, employeeId);
        clickUnobstructed(SEARCH_BUTTON);
        By row = row(employeeId);
        WaitUtils.navigationWait().until(d -> !d.findElements(row).isEmpty() || !d.findElements(NO_RECORDS).isEmpty());
        return this;
    }

    public boolean isListed(String employeeId) {
        return isPresentNow(row(employeeId));
    }

    public boolean isNoRecordsMessageShown() {
        return isPresentNow(NO_RECORDS);
    }

    public EmployeeDetailsPage openEmployee(String employeeId) {
        clickUnobstructed(rowAction(employeeId, "bi-pencil-fill"));
        EmployeeDetailsPage details = new EmployeeDetailsPage(driver);
        details.waitUntilLoaded();
        return details;
    }

    public EmployeeListPage deleteEmployee(String employeeId) {
        clickUnobstructed(rowAction(employeeId, "bi-trash"));
        click(CONFIRM_DELETE);
        waitForSuccessToast();
        return this;
    }

    private static By row(String employeeId) {
        return By.xpath(rowXpath(employeeId));
    }

    private static By rowAction(String employeeId, String iconClass) {
        return By.xpath(rowXpath(employeeId) + "//button[.//i[contains(@class,"
                + XPathUtils.literal(iconClass) + ")]]");
    }

    private static String rowXpath(String employeeId) {
        return "//div[contains(@class,'oxd-table-card')][.//div[normalize-space()=" + XPathUtils.literal(employeeId)
                + "]]";
    }
}
