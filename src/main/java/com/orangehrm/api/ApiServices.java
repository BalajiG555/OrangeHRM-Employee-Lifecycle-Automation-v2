package com.orangehrm.api;

import com.framework.api.AuthStrategy;
import com.framework.config.ConfigKeys;
import com.framework.config.ConfigReader;
import com.orangehrm.api.auth.AuthStrategyFactory;
import com.orangehrm.services.EmployeeService;

/**
 * Scenario-scoped access point for API clients. One authenticated session is created lazily and shared
 * by every client in the scenario (injected by Cucumber's PicoContainer, so each scenario - and therefore
 * each parallel thread - gets its own instance).
 */
public class ApiServices {

    private AuthStrategy authStrategy;
    private EmployeeService employeeService;
    private UserApiClient userApiClient;

    public synchronized EmployeeService employees() {
        if (employeeService == null) {
            employeeService = new EmployeeService(new EmployeeApiClient(baseUri(), auth()));
        }
        return employeeService;
    }

    public synchronized UserApiClient users() {
        if (userApiClient == null) {
            userApiClient = new UserApiClient(baseUri(), auth());
        }
        return userApiClient;
    }

    private AuthStrategy auth() {
        if (authStrategy == null) {
            authStrategy = AuthStrategyFactory.fromConfig(baseUri());
        }
        return authStrategy;
    }

    private static String baseUri() {
        return ConfigReader.get(ConfigKeys.API_BASE_URL, ConfigReader.getRequired(ConfigKeys.APP_BASE_URL));
    }
}
