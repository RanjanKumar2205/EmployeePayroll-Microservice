package org.example.springcloudgateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Binds gateway.* properties from application-dev.properties.
 *
 * gateway.jwks-uri    — URL of auth-service's JWKS endpoint (fetched once at startup)
 * gateway.public-paths — comma-separated paths that bypass JWT validation
 */
@Component
@ConfigurationProperties(prefix = "gateway")
public class ApplicationGatewayProperties {

    private String jwksUri;
    private String publicPaths = "";

    public String getJwksUri() { return jwksUri; }
    public void setJwksUri(String jwksUri) { this.jwksUri = jwksUri; }

    public String getPublicPaths() { return publicPaths; }
    public void setPublicPaths(String publicPaths) { this.publicPaths = publicPaths; }

    public List<String> getPublicPathList() {
        return Arrays.stream(publicPaths.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
