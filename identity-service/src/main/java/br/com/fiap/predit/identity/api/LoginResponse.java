package br.com.fiap.predit.identity.api;

import java.time.Instant;

public record LoginResponse(String accessToken, String tokenType, Instant expiresAt, UserResponse user) {
}
