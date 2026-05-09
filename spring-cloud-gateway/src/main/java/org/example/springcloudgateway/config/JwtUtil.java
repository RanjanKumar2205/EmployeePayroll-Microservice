package org.example.springcloudgateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@Component
public class JwtUtil {
    private final SecretKey signingKey;

    public JwtUtil(JwtProperties props) {
        this.signingKey = Keys.hmacShaKeyFor(Arrays.toString(Base64.getDecoder().decode(props.getSecret())).getBytes(StandardCharsets.UTF_8));
    }

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
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
