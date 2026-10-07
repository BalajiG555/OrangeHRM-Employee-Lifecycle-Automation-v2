package com.orangehrm.services;

import com.orangehrm.api.EmployeeApiClient;
import com.orangehrm.models.ApiEmployee;
import com.orangehrm.models.ApiJobDetails;
import com.orangehrm.models.Employee;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Business-level employee operations on top of {@link EmployeeApiClient}: turns raw responses into domain
 * objects. Contains no assertions - verification belongs to the step definitions.
 */
public class EmployeeService {

    private final EmployeeApiClient client;

    public EmployeeService(EmployeeApiClient client) {
        this.client = client;
    }

    /**
     * Creates the employee via API and stores the returned {@code empNumber} on it.
     */
    public Employee create(Employee employee) {
        int empNumber = client.createEmployee(employee).jsonPath().getInt("data.empNumber");
        employee.setEmpNumber(empNumber);
        return employee;
    }

    /**
     * Finds an employee by exact business employee id (the API search is a "contains" match).
     */
    public Optional<ApiEmployee> findByEmployeeId(String employeeId) {
        List<Map<String, Object>> matches = client.searchEmployees(employeeId).jsonPath().getList("data");
        if (matches == null) {
            return Optional.empty();
        }
        return matches.stream()
                .filter(row -> Objects.equals(employeeId, row.get("employeeId")))
                .findFirst()
                .map(row -> new ApiEmployee(
                        ((Number) row.get("empNumber")).intValue(),
                        (String) row.get("employeeId"),
                        (String) row.get("firstName"),
                        (String) row.get("lastName")));
    }

    public boolean exists(String employeeId) {
        return findByEmployeeId(employeeId).isPresent();
    }

    /**
     * Raw employee response, for status / schema validation by the caller.
     */
    public Response fetchEmployee(int empNumber) {
        return client.getEmployee(empNumber);
    }

    /**
     * Raw job-details response, for status / schema validation by the caller.
     */
    public Response fetchJobDetails(int empNumber) {
        return client.getJobDetails(empNumber);
    }

    public void delete(int empNumber) {
        client.deleteEmployees(List.of(empNumber));
    }

    /**
     * Maps an employee response body to {@link ApiEmployee}.
     */
    public static ApiEmployee toApiEmployee(Response response) {
        JsonPath json = response.jsonPath();
        return new ApiEmployee(
                json.getInt("data.empNumber"),
                json.getString("data.employeeId"),
                json.getString("data.firstName"),
                json.getString("data.lastName"));
    }

    /**
     * Maps a job-details response body to {@link ApiJobDetails}.
     */
    public static ApiJobDetails toApiJobDetails(Response response) {
        JsonPath json = response.jsonPath();
        return new ApiJobDetails(json.getString("data.jobTitle.title"), json.getString("data.empStatus.name"));
    }
}
