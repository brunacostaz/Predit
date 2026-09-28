package br.com.fiap.predit.risk.support;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/** Emite JWTs reais, no mesmo formato do Identity Service, para testar a validacao sem mocks. */
public final class TestTokens {
    public static final String SECRET = "unit-test-secret-with-at-least-32-characters-long";
    public static final String ISSUER = "predit-identity";

    private TestTokens() {}

    public static String valid(String role) {
        return sign(SECRET, ISSUER, List.of(role), Instant.now().plus(Duration.ofMinutes(30)));
    }

    public static String bearer(String role) {
        return "Bearer " + valid(role);
    }

    public static String sign(String secret, String issuer, List<String> roles, Instant expiresAt) {
        try {
            JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                    .subject(UUID.randomUUID().toString())
                    .issuer(issuer)
                    .issueTime(Date.from(expiresAt.minus(Duration.ofMinutes(30))))
                    .expirationTime(Date.from(expiresAt))
                    .claim("email", "user@predit.com.br")
                    .claim("name", "Test User");
            if (roles != null) claims.claim("roles", roles);
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims.build());
            jwt.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
