package com.fiap.zenith.core.application.dto;

import com.fiap.zenith.core.domain.entity.Farm;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representação de saída de uma fazenda. Nunca serializamos a entidade JPA direto na API.
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

    public static FarmResponse from(Farm farm) {
        return new FarmResponse(
                farm.getId(),
                farm.getCode(),
                farm.getUserId(),
                farm.getName(),
                farm.getCarRegistration(),
                farm.getNirf(),
                farm.getLatitude(),
                farm.getLongitude(),
                farm.getTotalAreaHectares(),
                farm.getState(),
                farm.getBiomeId(),
                farm.getPropertyType(),
                farm.getPolygonWkt(),
                farm.getActive(),
                farm.getCreatedAt(),
                farm.getEditedAt()
        );
    }
}
