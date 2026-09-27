package br.com.fiap.predit.identity.api;

import br.com.fiap.predit.identity.domain.Role;
import br.com.fiap.predit.identity.domain.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String name, String email, Role role, boolean active, Instant createdAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(),
                user.isActive(), user.getCreatedAt());
    }
}
