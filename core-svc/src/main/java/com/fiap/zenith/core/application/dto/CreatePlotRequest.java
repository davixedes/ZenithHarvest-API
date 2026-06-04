package com.fiap.zenith.core.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePlotRequest(
        @NotNull UUID farmId,
        UUID cropId,
        @NotNull Integer plotSituationId,
        Integer productionSystemId,
        @Size(max = 40) String identifier,
        @Positive BigDecimal areaHectares,
        LocalDate plantingDate,
        LocalDate estimatedHarvestDate,
        @Positive Integer cycleDays,
        @Size(max = 100) String seedVariety,
        String polygonWkt
) {}
