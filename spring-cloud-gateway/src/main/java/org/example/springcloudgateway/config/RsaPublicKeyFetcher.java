package org.example.springcloudgateway.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Configuration
public class RsaPublicKeyFetcher {

    private static final Logger log = LoggerFactory.getLogger(RsaPublicKeyFetcher.class);

    @Bean
    public RSAPublicKey rsaPublicKey(ApplicationGatewayProperties props) throws Exception {
        log.info("Fetching RSA public key from JWKS endpoint: {}", props.getJwksUri());

        String jwksJson = WebClient.create()
                .get()
                .uri(props.getJwksUri())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (jwksJson == null || jwksJson.isBlank()) {
            throw new IllegalStateException(
                    "Empty JWKS response from auth-service at: " + props.getJwksUri());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jwksJson);
        JsonNode firstKey = root.get("keys").get(0);

        String n = firstKey.get("n").asText();
        String e = firstKey.get("e").asText();

        Base64.Decoder dec = Base64.getUrlDecoder();
        BigInteger modulus  = new BigInteger(1, dec.decode(n));
        BigInteger exponent = new BigInteger(1, dec.decode(e));

        RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new RSAPublicKeySpec(modulus, exponent));

        log.info("RSA public key fetched and cached successfully");
        return publicKey;
    }
}
