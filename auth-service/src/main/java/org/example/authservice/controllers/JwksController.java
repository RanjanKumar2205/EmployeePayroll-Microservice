package org.example.authservice.controllers;

import org.example.authservice.config.JwtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Publishes the RSA public key in JWKS (JSON Web Key Set) format.
 *
 * The gateway fetches GET /api/v1/auth/.well-known/jwks.json once at startup
 * and caches the public key. It then verifies every JWT locally using that key —
 * no shared secret, no per-request call to auth-service.
 *
 * This endpoint is public (no JWT required) — see SecurityConfig.
 */
@RestController
@RequestMapping("/api/v1/auth/.well-known")
public class JwksController {

    private final JwtService jwtService;

    public JwksController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Returns the public key as a standard JWKS document.
     *
     * Response shape:
     * {
     *   "keys": [{
     *     "kty": "RSA",
     *     "use": "sig",
     *     "alg": "RS256",
     *     "n":   "<base64url-encoded modulus>",
     *     "e":   "<base64url-encoded exponent>"
     *   }]
     * }
     */
    @GetMapping("/jwks.json")
    public Map<String, Object> jwks() {
        RSAPublicKey publicKey = jwtService.getPublicKey();
        Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();

        Map<String, Object> jwk = Map.of(
                "kty", "RSA",
                "use", "sig",
                "alg", "RS256",
                "n",   enc.encodeToString(publicKey.getModulus().toByteArray()),
                "e",   enc.encodeToString(publicKey.getPublicExponent().toByteArray())
        );

        return Map.of("keys", List.of(jwk));
    }
}
