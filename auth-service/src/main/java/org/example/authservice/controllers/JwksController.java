package org.example.authservice.controllers;

import org.example.authservice.config.JwtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/.well-known")
public class JwksController {

    private final JwtService jwtService;

    public JwksController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

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
