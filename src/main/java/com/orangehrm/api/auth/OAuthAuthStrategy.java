package com.orangehrm.api.auth;

import com.framework.api.AuthStrategy;
import com.framework.config.ConfigKeys;
import com.framework.config.ConfigReader;
import com.framework.exceptions.ApiException;
import com.orangehrm.api.ApiEndpoints;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * OAuth 2.0 bearer-token authentication against OrangeHRM's {@code /oauth2/token} endpoint.
 *
 * <p>Prefers a pre-issued {@code api.access.token}. Otherwise it exchanges {@code api.authorization.code}
 * (optionally with a PKCE {@code api.code.verifier}). Note that authorization codes are single-use, so for
 * CI the access-token option (or the default form-login strategy) is the reliable choice.</p>
 */
public final class OAuthAuthStrategy implements AuthStrategy {

    private static final int HTTP_OK = 200;

    private final String baseUri;
    private String accessToken;

    public OAuthAuthStrategy(String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public RequestSpecification authenticate(RequestSpecification request) {
        return request.header("Authorization", "Bearer " + accessToken());
    }

    @Override
    public String name() {
        return "oauth2";
    }

    private synchronized String accessToken() {
        if (accessToken == null) {
            String configured = ConfigReader.get(ConfigKeys.API_ACCESS_TOKEN);
            accessToken = configured != null ? configured : exchangeAuthorizationCode();
        }
        return accessToken;
    }

    private String exchangeAuthorizationCode() {
        RequestSpecification token = RestAssured.given().baseUri(baseUri)
                .contentType(ContentType.URLENC)
                .formParam("grant_type", "authorization_code")
                .formParam("code", ConfigReader.getRequired(ConfigKeys.API_AUTHORIZATION_CODE))
                .formParam("client_id", ConfigReader.getRequired(ConfigKeys.API_CLIENT_ID))
                .formParam("redirect_uri", ConfigReader.getRequired(ConfigKeys.API_REDIRECT_URI));
        String clientSecret = ConfigReader.get(ConfigKeys.API_CLIENT_SECRET);
        if (clientSecret != null) {
            token.formParam("client_secret", clientSecret);
        }
        String codeVerifier = ConfigReader.get(ConfigKeys.API_CODE_VERIFIER);
        if (codeVerifier != null) {
            token.formParam("code_verifier", codeVerifier);
        }
        Response response = token.post(ApiEndpoints.OAUTH_TOKEN);
        if (response.statusCode() != HTTP_OK) {
            throw new ApiException("OAuth token request failed with HTTP " + response.statusCode()
                    + ". Authorization codes are single-use - supply a fresh code or api.access.token.");
        }
        String issued = response.jsonPath().getString("access_token");
        if (issued == null || issued.isBlank()) {
            throw new ApiException("OAuth token response did not contain an access_token.");
        }
        return issued;
    }
}
