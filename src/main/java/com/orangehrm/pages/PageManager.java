package com.orangehrm.pages;

import com.framework.driver.DriverFactory;
import com.orangehrm.pages.components.SideMenu;

import org.openqa.selenium.WebDriver;

/**
 * Single entry point for page objects in step definitions. Pages are created on demand against the
 * current thread's driver, so a PageManager can be injected before the browser starts.
 */
public class PageManager {

    public LoginPage loginPage() {
        return new LoginPage(driver());
    }

    public DashboardPage dashboardPage() {
        return new DashboardPage(driver());
    }

    public SideMenu sideMenu() {
        return new SideMenu(driver());
    }

    public EmployeeListPage employeeListPage() {
        return new EmployeeListPage(driver());
    }

    public AddEmployeePage addEmployeePage() {
        return new AddEmployeePage(driver());
    }

    public EmployeeDetailsPage employeeDetailsPage() {
        return new EmployeeDetailsPage(driver());
    }

    public SystemUsersPage systemUsersPage() {
        return new SystemUsersPage(driver());
    }

    private static WebDriver driver() {
        return DriverFactory.getDriver();
    }
}
