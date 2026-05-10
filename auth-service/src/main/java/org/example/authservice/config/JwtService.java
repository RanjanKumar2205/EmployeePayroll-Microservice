package org.example.authservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import org.example.authservice.security.UserPrincipal;
import org.example.authservice.utils.GlobalConstantsUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final KeyPair   keyPair;
    @Getter
    private final long      expirationMs;

    public JwtService(KeyPair keyPair,
                      @Value("${jwt.expiration-ms}") long expirationMs) {
        this.keyPair     = keyPair;
        this.expirationMs = expirationMs;
    }

    public String generateToken(Map<String, Object> claims, UserPrincipal userPrincipal) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(userPrincipal.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }

    public String extractSubject(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).get(GlobalConstantsUtil.ROLE_KEY, String.class);
    }

    public RSAPublicKey getPublicKey() {
        return (RSAPublicKey) keyPair.getPublic();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(keyPair.getPublic())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
