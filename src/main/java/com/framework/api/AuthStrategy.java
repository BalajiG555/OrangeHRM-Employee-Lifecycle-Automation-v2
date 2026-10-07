package com.framework.api;

import io.restassured.specification.RequestSpecification;

/**
 * Strategy for authenticating API requests (session cookie, bearer token, ...). New authentication
 * schemes are added by implementing this interface - API clients never change.
 */
public interface AuthStrategy {

    /**
     * Decorates the request with credentials, acquiring them lazily on first use.
     */
    RequestSpecification authenticate(RequestSpecification request);

    /**
     * Human-readable name used in logs and reports.
     */
    String name();
}
