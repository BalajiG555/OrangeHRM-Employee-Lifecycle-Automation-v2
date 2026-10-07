package com.orangehrm.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Employee test-data model shared by the UI and API layers. Loaded from {@code testdata/employee.json}
 * and made unique per scenario by {@link com.orangehrm.data.TestDataGenerator}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee {

    private String firstName;
    private String lastName;
    private String employeeId;
    private String jobTitle;
    private String employmentStatus;
    private String profilePicture;
    private Integer empNumber;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    /**
     * Classpath location of the profile picture to upload, e.g. {@code testdata/profile.png}.
     */
    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    /**
     * OrangeHRM's internal primary key, known once the employee exists in the backend.
     */
    public Integer getEmpNumber() {
        return empNumber;
    }

    public void setEmpNumber(Integer empNumber) {
        this.empNumber = empNumber;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return "Employee{" + getFullName() + ", employeeId=" + employeeId + ", empNumber=" + empNumber + "}";
    }
}
