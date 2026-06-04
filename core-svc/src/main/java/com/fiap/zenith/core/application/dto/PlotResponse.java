package com.fiap.zenith.core.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PlotResponse(
        UUID id,
        Integer code,
        UUID farmId,
        UUID cropId,
        Integer plotSituationId,
        Integer productionSystemId,
        String identifier,
        BigDecimal areaHectares,
        LocalDate plantingDate,
        LocalDate estimatedHarvestDate,
        Integer cycleDays,
        String seedVariety,
        String polygonWkt,
        OffsetDateTime createdAt,
        OffsetDateTime editedAt
) {}
