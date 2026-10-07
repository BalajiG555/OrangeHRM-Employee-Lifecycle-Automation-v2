package com.orangehrm.api;

import com.framework.api.ApiClient;
import com.framework.api.AuthStrategy;
import com.orangehrm.models.Employee;

import io.restassured.response.Response;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Low-level client for the PIM employee endpoints. Returns raw responses; interpretation lives in
 * {@link com.orangehrm.services.EmployeeService}.
 */
public class EmployeeApiClient extends ApiClient {

    private static final int SEARCH_PAGE_SIZE = 50;

    public EmployeeApiClient(String baseUri, AuthStrategy authStrategy) {
        super(baseUri, authStrategy);
    }

    /**
     * Creates an employee (same payload the Add Employee form sends).
     */
    public Response createEmployee(Employee employee) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", employee.getFirstName());
        body.put("middleName", "");
        body.put("lastName", employee.getLastName());
        body.put("employeeId", employee.getEmployeeId());
        body.put("empPicture", null);
        return expectSuccess(request().body(body).post(ApiEndpoints.EMPLOYEES), "Create employee");
    }

    /**
     * Server-side filtered search by employee id or name - avoids paging through every employee.
     */
    public Response searchEmployees(String nameOrId) {
        return expectSuccess(request()
                .queryParam("nameOrId", nameOrId)
                .queryParam("limit", SEARCH_PAGE_SIZE)
                .queryParam("offset", 0)
                .get(ApiEndpoints.EMPLOYEES), "Search employees");
    }

    /**
     * Returns the raw employee response (status not asserted, so callers can validate status and schema).
     */
    public Response getEmployee(int empNumber) {
        return request()
                .pathParam(ApiEndpoints.PATH_PARAM_EMP_NUMBER, empNumber)
                .queryParam("model", "detailed")
                .get(ApiEndpoints.EMPLOYEE);
    }

    /**
     * Returns the raw job-details response (status not asserted).
     */
    public Response getJobDetails(int empNumber) {
        return request()
                .pathParam(ApiEndpoints.PATH_PARAM_EMP_NUMBER, empNumber)
                .get(ApiEndpoints.EMPLOYEE_JOB_DETAILS);
    }

    /**
     * Deletes the given employees.
     */
    public Response deleteEmployees(Collection<Integer> empNumbers) {
        return expectSuccess(request().body(Map.of("ids", List.copyOf(empNumbers))).delete(ApiEndpoints.EMPLOYEES),
                "Delete employees");
    }
}
