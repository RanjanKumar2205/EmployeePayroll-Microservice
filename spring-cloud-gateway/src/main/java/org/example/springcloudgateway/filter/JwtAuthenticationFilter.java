package org.example.springcloudgateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.springcloudgateway.config.JwtProperties;
import org.example.springcloudgateway.config.JwtUtil;
import org.example.springcloudgateway.exception.GatewayErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Global pre-filter that validates a Bearer JWT on every request before
 * the gateway routes it to a downstream service.
 *
 * Flow:
 *   1. Skip validation for public paths (e.g. /actuator/health).
 *   2. Reject requests with no Authorization header → 401.
 *   3. Reject requests with an invalid / expired token → 401.
 *   4. On valid token: forward the request and propagate X-Auth-User header
 *      (subject extracted from token) downstream so services can identify the caller.
 *
 * Ordered.HIGHEST_PRECEDENCE ensures this runs before all other filters.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String BEARER_PREFIX = "Bearer ";
    /** Header propagated downstream containing the authenticated user's subject. */
    private static final String X_AUTH_USER_HEADER = "X-Auth-User";

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, JwtProperties jwtProperties) {
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
        // Register JavaTimeModule so Instant serialises as ISO-8601 string
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    // ── GlobalFilter ─────────────────────────────────────────────────────────

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // ── 1. Public path bypass ─────────────────────────────────────────────
        if (isPublicPath(path)) {
            log.debug("Public path – skipping JWT validation: {}", path);
            return chain.filter(exchange);
        }

        // ── 2. Extract Authorization header ──────────────────────────────────
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Missing or malformed Authorization header on path: {}", path);
            return rejectWith(exchange, HttpStatus.UNAUTHORIZED,
                    "JWT token is missing or malformed. "
                    + "Provide 'Authorization: Bearer <token>'");
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        // ── 3. Validate token ─────────────────────────────────────────────────
        if (!jwtUtil.isValid(token)) {
            log.warn("Invalid or expired JWT on path: {}", path);
            return rejectWith(exchange, HttpStatus.UNAUTHORIZED,
                    "JWT token is invalid or expired");
        }

        // ── 4. Forward with X-Auth-User header ───────────────────────────────
        String subject = jwtUtil.extractSubject(token);
        log.debug("JWT valid for subject '{}', routing to: {}", subject, path);

        ServerHttpRequest mutatedRequest = request.mutate()
                .header(X_AUTH_USER_HEADER, subject)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        // Run before all other filters (route filters, load-balancer, etc.)
        return Ordered.HIGHEST_PRECEDENCE;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Returns true if the request path starts with any configured public prefix.
     */
    private boolean isPublicPath(String path) {
        return jwtProperties.getPublicPathList().stream()
                .anyMatch(path::startsWith);
    }

    /**
     * Writes a JSON error response and completes the exchange without routing.
     */
    private Mono<Void> rejectWith(ServerWebExchange exchange,
                                  HttpStatus status,
                                  String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        GatewayErrorResponse body = GatewayErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getURI().getPath()
        );

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            // Fallback: plain text — should never happen
            bytes = ("{\"error\":\"" + status.getReasonPhrase() + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
