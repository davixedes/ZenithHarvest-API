package com.fiap.zenith.core.application.mapper;

import com.fiap.zenith.core.application.dto.FarmResponse;
import com.fiap.zenith.core.domain.entity.Farm;
import org.springframework.stereotype.Component;

/**
 * Converte a entidade {@link Farm} no DTO de saída {@link FarmResponse}. Concentra o
 * acoplamento entity↔DTO aqui (a seam de mapeamento), mantendo o record puro e a entidade
 * livre de conhecer a camada de API.
 */
@Component
public class FarmMapper {

    public FarmResponse toResponse(Farm farm) {
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
