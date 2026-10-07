package com.framework.api;

import com.framework.exceptions.ApiException;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Objects;

/**
 * Base class for API clients. Provides a pre-configured request (base URI, JSON, auth, logging and
 * Allure request/response attachments) so concrete clients only describe endpoints.
 *
 * <p>Redirects are not followed: an expired session answers with a 302 to the login page, and we want
 * that surfaced as a clear failure rather than parsed as a 200 HTML page.</p>
 */
public abstract class ApiClient {

    private static final int HTTP_OK_MIN = 200;
    private static final int HTTP_OK_MAX = 299;
    private static final int MAX_BODY_IN_ERROR = 500;

    private final String baseUri;
    private final AuthStrategy authStrategy;

    protected ApiClient(String baseUri, AuthStrategy authStrategy) {
        this.baseUri = Objects.requireNonNull(baseUri, "baseUri");
        this.authStrategy = Objects.requireNonNull(authStrategy, "authStrategy");
    }

    /**
     * Returns an authenticated, fully configured request specification.
     */
    protected RequestSpecification request() {
        RequestSpecification spec = RestAssured.given()
                .baseUri(baseUri)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filter(new ApiLoggingFilter())
                .filter(new AllureRestAssured())
                .redirects().follow(false);
        return authStrategy.authenticate(spec);
    }

    /**
     * Throws an {@link ApiException} with status and a body excerpt unless the response is 2xx.
     */
    protected static Response expectSuccess(Response response, String operation) {
        int status = response.statusCode();
        if (status < HTTP_OK_MIN || status > HTTP_OK_MAX) {
            String body = response.asString();
            String excerpt = body.length() > MAX_BODY_IN_ERROR ? body.substring(0, MAX_BODY_IN_ERROR) + "..." : body;
            throw new ApiException(operation + " failed with HTTP " + status + ": " + excerpt);
        }
        return response;
    }
}
