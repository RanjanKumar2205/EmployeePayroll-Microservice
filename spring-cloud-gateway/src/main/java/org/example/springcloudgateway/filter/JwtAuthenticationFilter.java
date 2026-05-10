package org.example.springcloudgateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.springcloudgateway.config.ApplicationGatewayProperties;
import org.example.springcloudgateway.config.JwtVerifier;
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

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String BEARER_PREFIX    = "Bearer ";
    private static final String X_AUTH_USER      = "X-Auth-User";
    private static final String X_AUTH_ROLE      = "X-Auth-Role";
    private static final String ROLE_KEY         = "ROLE";

    private final JwtVerifier      jwtVerifier;
    private final ApplicationGatewayProperties gatewayProperties;
    private final ObjectMapper     objectMapper;

    public JwtAuthenticationFilter(JwtVerifier jwtVerifier,
                                   ApplicationGatewayProperties gatewayProperties) {
        this.jwtVerifier       = jwtVerifier;
        this.gatewayProperties = gatewayProperties;
        this.objectMapper      = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            log.debug("Public path — skipping JWT validation: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Missing or malformed Authorization header: {}", path);
            return rejectWith(exchange, HttpStatus.UNAUTHORIZED,
                    "JWT token is missing. Provide 'Authorization: Bearer <token>'");
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        if (!jwtVerifier.isValid(token)) {
            log.warn("Invalid or expired JWT on path: {}", path);
            return rejectWith(exchange, HttpStatus.UNAUTHORIZED,
                    "JWT token is invalid or expired");
        }

        String subject = jwtVerifier.extractSubject(token);
        String role    = jwtVerifier.extractAllClaims(token).get(ROLE_KEY, String.class);

        log.debug("JWT valid — subject='{}' role='{}' path='{}'", subject, role, path);

        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header(X_AUTH_USER, subject)
                .header(X_AUTH_ROLE, role != null ? role : "")
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isPublicPath(String path) {
        return gatewayProperties.getPublicPathList().stream().anyMatch(path::startsWith);
    }

    private Mono<Void> rejectWith(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        GatewayErrorResponse body = GatewayErrorResponse.of(
                status.value(), status.getReasonPhrase(), message,
                exchange.getRequest().getURI().getPath());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = ("{\"error\":\"" + status.getReasonPhrase() + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
