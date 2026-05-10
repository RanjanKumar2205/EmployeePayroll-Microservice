package org.example.springcloudgateway.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GatewayErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static GatewayErrorResponse of(int status, String error, String message, String path) {
        return new GatewayErrorResponse(Instant.now(), status, error, message, path);
    }
}
