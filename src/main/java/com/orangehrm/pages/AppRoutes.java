package com.orangehrm.pages;

import java.util.regex.Pattern;

/**
 * OrangeHRM UI routes relative to {@code app.base.url}.
 */
public final class AppRoutes {

    public static final String LOGIN = "/auth/login";
    public static final String DASHBOARD = "/dashboard/index";
    public static final String SYSTEM_USERS = "/admin/viewSystemUsers";
    public static final Pattern PERSONAL_DETAILS = Pattern.compile("/pim/viewPersonalDetails/empNumber/(\\d+)");

    private AppRoutes() {
    }
}
