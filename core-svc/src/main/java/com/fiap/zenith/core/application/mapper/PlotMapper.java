package com.fiap.zenith.core.application.mapper;

import com.fiap.zenith.core.application.dto.PlotResponse;
import com.fiap.zenith.core.domain.entity.Plot;
import org.springframework.stereotype.Component;

@Component
public class PlotMapper {

    public PlotResponse toResponse(Plot p) {
        return new PlotResponse(
                p.getId(), p.getCode(), p.getFarmId(), p.getCropId(),
                p.getPlotSituationId(), p.getProductionSystemId(), p.getIdentifier(),
                p.getAreaHectares(), p.getPlantingDate(), p.getEstimatedHarvestDate(),
                p.getCycleDays(), p.getSeedVariety(), p.getPolygonWkt(),
                p.getCreatedAt(), p.getEditedAt());
    }
}
