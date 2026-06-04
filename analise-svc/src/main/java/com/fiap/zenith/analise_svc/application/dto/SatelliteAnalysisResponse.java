package com.fiap.zenith.analise_svc.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SatelliteAnalysisResponse(
        UUID id,
        UUID claimId,
        UUID plotId,
        Integer satelliteSourceId,
        Integer satelliteClassId,
        LocalDate imageDate,
        BigDecimal meanNdvi,
        BigDecimal meanEvi,
        BigDecimal cloudCoveragePct,
        BigDecimal affectedAreaM2,
        BigDecimal mlConfidence,
        OffsetDateTime processedAt,
        OffsetDateTime createdAt
) {}
