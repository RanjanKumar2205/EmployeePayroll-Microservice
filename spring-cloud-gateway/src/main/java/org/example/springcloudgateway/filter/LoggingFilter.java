package org.example.springcloudgateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global post-filter that logs every routed request with method, path,
 * downstream service, response status, and latency.
 *
 * Runs at HIGHEST_PRECEDENCE + 1 so it wraps the JWT filter and captures
 * both allowed and rejected requests in the log.
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startMs = System.currentTimeMillis();

        log.info("→ {} {}", request.getMethod(), request.getURI().getPath());

        return chain.filter(exchange)
                .doFinally(signal -> {
                    long latencyMs = System.currentTimeMillis() - startMs;
                    int statusCode = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;
                    log.info("← {} {} | status={} | {}ms",
                            request.getMethod(),
                            request.getURI().getPath(),
                            statusCode,
                            latencyMs);
                });
    }

    @Override
    public int getOrder() {
        // Just after JWT filter so both allowed and rejected calls are logged
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
