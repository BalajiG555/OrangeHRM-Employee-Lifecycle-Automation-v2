package com.orangehrm.api.auth;

import com.framework.api.AuthStrategy;
import com.framework.driver.DriverFactory;
import com.framework.exceptions.ApiException;

import io.restassured.specification.RequestSpecification;
import org.openqa.selenium.Cookie;

/**
 * Reuses the session cookie of the browser that is currently logged in. Useful when form login is not
 * possible (e.g. SSO), at the cost of requiring a logged-in browser for every API call.
 */
public final class SessionCookieAuthStrategy implements AuthStrategy {

    @Override
    public RequestSpecification authenticate(RequestSpecification request) {
        Cookie cookie = DriverFactory.getDriver().manage().getCookieNamed(FormLoginAuthStrategy.SESSION_COOKIE);
        if (cookie == null) {
            throw new ApiException("No '" + FormLoginAuthStrategy.SESSION_COOKIE
                    + "' cookie in the browser - log in through the UI before calling the API.");
        }
        return request.cookie(cookie.getName(), cookie.getValue());
    }

    @Override
    public String name() {
        return "browser-session";
    }
}
