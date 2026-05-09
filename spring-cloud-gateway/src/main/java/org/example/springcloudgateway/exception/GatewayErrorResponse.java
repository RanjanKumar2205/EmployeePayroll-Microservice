package org.example.springcloudgateway.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Uniform JSON error body returned by the gateway for 401 / 403 responses.
 *
 * Example:
 * {
 *   "timestamp": "2026-05-08T10:00:00Z",
 *   "status": 401,
 *   "error": "Unauthorized",
 *   "message": "JWT token is missing",
 *   "path": "/api/employees"
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GatewayErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
    /** Convenience factory — sets timestamp automatically. */
    public static GatewayErrorResponse of(int status, String error, String message, String path) {
        return new GatewayErrorResponse(Instant.now(), status, error, message, path);
    }
}
