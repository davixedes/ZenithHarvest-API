package com.fiap.zenith.core.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representação de saída de uma fazenda. Nunca serializamos a entidade JPA direto na API.
 * O mapeamento entity→DTO vive em {@code FarmMapper} (record puro, sem conhecer o domínio).
 */
public record FarmResponse(
        UUID id,
        Integer code,
        UUID userId,
        String name,
        String carRegistration,
        String nirf,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal totalAreaHectares,
        String state,
        Integer biomeId,
        String propertyType,
        String polygonWkt,
        Boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime editedAt
) {
}
