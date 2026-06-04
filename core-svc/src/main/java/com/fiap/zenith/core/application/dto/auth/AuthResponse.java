package com.fiap.zenith.core.application.dto.auth;

import java.util.UUID;

/**
 * Resposta de login: o token JWT e seus metadados.
 */
public record AuthResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        UUID userId,
        String email
) {

    public static AuthResponse bearer(String token, long expiresInSeconds, UUID userId, String email) {
        return new AuthResponse(token, "Bearer", expiresInSeconds, userId, email);
    }
}
