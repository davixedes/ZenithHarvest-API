package com.fiap.zenith.core.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representação de saída de um usuário. NUNCA inclui senha/secret (que vivem em Credential).
 * O mapeamento entity→DTO vive em {@code UserMapper} (record puro, sem conhecer o domínio).
 */
public record UserResponse(
        UUID id,
        Integer code,
        String cpf,
        String name,
        String lastName,
        String email,
        String phone,
        UUID addressId,
        OffsetDateTime lastLoginAt,
        OffsetDateTime createdAt
) {
}
