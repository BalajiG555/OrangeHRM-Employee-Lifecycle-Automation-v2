package com.orangehrm.pages;

import com.framework.utils.ResourceUtils;
import com.framework.utils.WaitUtils;
import com.orangehrm.models.Employee;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * PIM &gt; Add Employee.
 */
public class AddEmployeePage extends OrangeHrmPage {

    private static final String PIM_MENU = "PIM";
    private static final By ADD_EMPLOYEE_TAB = By.xpath("//a[normalize-space()='Add Employee']");
    private static final By FIRST_NAME = By.name("firstName");
    private static final By LAST_NAME = By.name("lastName");
    private static final By EMPLOYEE_ID = OxdLocators.inputByLabel("Employee Id");
    private static final By PROFILE_PICTURE = By.cssSelector("input[type='file']");
    private static final By SAVE_BUTTON = OxdLocators.buttonByText("Save");

    public AddEmployeePage(WebDriver driver) {
        super(driver);
    }

    public AddEmployeePage open() {
        sideMenu().open(PIM_MENU);
        click(ADD_EMPLOYEE_TAB);
        waitForServer(FIRST_NAME);
        return this;
    }

    /**
     * Fills the form, uploads the picture, saves and returns the new employee's details page.
     */
    public EmployeeDetailsPage createEmployee(Employee employee) {
        type(FIRST_NAME, employee.getFirstName());
        type(LAST_NAME, employee.getLastName());
        enterEmployeeId(employee.getEmployeeId());
        if (employee.getProfilePicture() != null) {
            driver.findElement(PROFILE_PICTURE).sendKeys(ResourceUtils.absolutePath(employee.getProfilePicture()));
        }
        clickUnobstructed(SAVE_BUTTON);
        EmployeeDetailsPage details = new EmployeeDetailsPage(driver);
        details.waitUntilLoaded();
        return details;
    }

    /**
     * OrangeHRM pre-fills an auto-generated id asynchronously; wait for it so it cannot overwrite ours.
     */
    private void enterEmployeeId(String employeeId) {
        WaitUtils.navigationWait().until(d -> !getValue(EMPLOYEE_ID).isEmpty());
        type(EMPLOYEE_ID, employeeId);
    }
}
