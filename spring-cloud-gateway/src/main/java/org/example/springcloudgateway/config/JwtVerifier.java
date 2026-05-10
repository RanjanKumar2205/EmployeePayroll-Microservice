package org.example.springcloudgateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;

/**
 * Stateless JWT verifier using the cached RSA public key.
 *
 * Replaces the old JwtUtil that used a shared HMAC secret.
 * Verification is pure CPU — no I/O, no DB, no auth-service call.
 */
@Component
public class JwtVerifier {

    private final RSAPublicKey publicKey;

    public JwtVerifier(RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Returns true if the token has a valid RS256 signature and is not expired.
     * False on any JwtException (tampered, expired, malformed).
     */
    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractSubject(String token) {
        return getClaims(token).getSubject();
    }

    public Claims extractAllClaims(String token) {
        return getClaims(token);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
