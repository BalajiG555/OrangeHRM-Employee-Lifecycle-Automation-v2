package com.orangehrm.stepdefinitions;

import com.orangehrm.models.UserRole;

import io.cucumber.java.ParameterType;

/**
 * Custom Cucumber parameter types, so steps receive typed values instead of parsing strings themselves.
 */
public class ParameterTypes {

    @ParameterType("Admin|ESS")
    public UserRole role(String label) {
        return UserRole.fromLabel(label);
    }

    @ParameterType("visible|hidden")
    public Boolean visibility(String value) {
        return "visible".equals(value);
    }

    @ParameterType("granted|denied")
    public Boolean access(String value) {
        return "granted".equals(value);
    }
}
