package com.framework.api;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs one concise line per API call (method, URI, status, duration). Request bodies are deliberately
 * not logged so credentials can never leak into CI logs.
 */
public final class ApiLoggingFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ApiLoggingFilter.class);

    @Override
    public Response filter(FilterableRequestSpecification request,
                           FilterableResponseSpecification responseSpec,
                           FilterContext context) {
        long start = System.nanoTime();
        Response response = context.next(request, responseSpec);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        LOG.info("API {} {} -> {} ({} ms)", request.getMethod(), request.getURI(), response.statusCode(), elapsedMs);
        return response;
    }
}
