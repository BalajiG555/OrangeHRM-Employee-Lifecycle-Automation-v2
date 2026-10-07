package com.orangehrm.api;

/**
 * OrangeHRM endpoint paths, relative to {@code api.base.url} (which defaults to {@code app.base.url}).
 */
public final class ApiEndpoints {

    public static final String LOGIN_PAGE = "/auth/login";
    public static final String LOGIN_VALIDATE = "/auth/validate";
    public static final String OAUTH_TOKEN = "/oauth2/token";

    public static final String EMPLOYEES = "/api/v2/pim/employees";
    public static final String EMPLOYEE = "/api/v2/pim/employees/{empNumber}";
    public static final String EMPLOYEE_JOB_DETAILS = "/api/v2/pim/employees/{empNumber}/job-details";
    public static final String SYSTEM_USERS = "/api/v2/admin/users";

    public static final String PATH_PARAM_EMP_NUMBER = "empNumber";

    private ApiEndpoints() {
    }
}
