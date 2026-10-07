package com.orangehrm.models;

import java.util.Arrays;

/**
 * OrangeHRM user roles with their backend role ids.
 */
public enum UserRole {
    ADMIN("Admin", 1),
    ESS("ESS", 2);

    private final String label;
    private final int roleId;

    UserRole(String label, int roleId) {
        this.label = label;
        this.roleId = roleId;
    }

    public String label() {
        return label;
    }

    public int roleId() {
        return roleId;
    }

    /**
     * Resolves a role from its UI label ({@code Admin}, {@code ESS}).
     */
    public static UserRole fromLabel(String label) {
        return Arrays.stream(values())
                .filter(role -> role.label.equalsIgnoreCase(label.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + label));
    }
}
