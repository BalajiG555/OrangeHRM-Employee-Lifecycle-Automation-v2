package com.orangehrm.api.auth;

import com.framework.api.AuthStrategy;
import com.framework.exceptions.ApiException;
import com.orangehrm.api.ApiEndpoints;
import com.orangehrm.models.Credentials;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure-API login: performs the same form login the browser does and reuses the resulting session
 * cookie for {@code /api/v2} calls.
 *
 * <p>This is the default because it needs no extra secrets (it uses the admin credentials the UI tests
 * already have), works before any browser exists (API-driven test-data setup) and is independent of UI
 * state (cleanup still works after a scenario logged out or switched user). Requests are deliberately not
 * attached to the Allure report so the password can never appear in it.</p>
 */
public final class FormLoginAuthStrategy implements AuthStrategy {

    static final String SESSION_COOKIE = "orangehrm";
    private static final Pattern CSRF_TOKEN = Pattern.compile(":token=\"(?:&quot;)?([^\"&]+)(?:&quot;)?\"");
    private static final int HTTP_OK = 200;
    private static final int HTTP_FOUND = 302;

    private final String baseUri;
    private final Credentials credentials;
    private String sessionCookie;

    public FormLoginAuthStrategy(String baseUri, Credentials credentials) {
        this.baseUri = baseUri;
        this.credentials = credentials;
    }

    @Override
    public RequestSpecification authenticate(RequestSpecification request) {
        return request.cookie(SESSION_COOKIE, sessionCookie());
    }

    @Override
    public String name() {
        return "form-login";
    }

    private synchronized String sessionCookie() {
        if (sessionCookie == null) {
            sessionCookie = login();
        }
        return sessionCookie;
    }

    private String login() {
        Response loginPage = RestAssured.given().baseUri(baseUri).redirects().follow(false)
                .get(ApiEndpoints.LOGIN_PAGE);
        if (loginPage.statusCode() != HTTP_OK) {
            throw new ApiException("API login: login page returned HTTP " + loginPage.statusCode());
        }
        Matcher matcher = CSRF_TOKEN.matcher(loginPage.asString());
        if (!matcher.find()) {
            throw new ApiException("API login: CSRF token not found on the login page. "
                    + "Set api.auth.mode=session to reuse the browser session instead.");
        }
        Map<String, String> cookies = new HashMap<>(loginPage.getCookies());

        Response validate = RestAssured.given().baseUri(baseUri).redirects().follow(false)
                .cookies(cookies)
                .contentType(ContentType.URLENC)
                .formParam("_token", matcher.group(1))
                .formParam("username", credentials.username())
                .formParam("password", credentials.password())
                .post(ApiEndpoints.LOGIN_VALIDATE);

        String location = validate.getHeader("Location");
        if (validate.statusCode() != HTTP_FOUND || location == null || location.contains(ApiEndpoints.LOGIN_PAGE)) {
            throw new ApiException("API login failed for user '" + credentials.username()
                    + "' (HTTP " + validate.statusCode() + ", redirect=" + location + ").");
        }
        String rotated = validate.getCookie(SESSION_COOKIE);
        String cookie = rotated != null ? rotated : cookies.get(SESSION_COOKIE);
        if (cookie == null || cookie.isBlank()) {
            throw new ApiException("API login succeeded but no '" + SESSION_COOKIE + "' session cookie was returned.");
        }
        return cookie;
    }
}
