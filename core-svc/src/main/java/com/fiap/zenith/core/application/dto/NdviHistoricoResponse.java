package com.fiap.zenith.core.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Leitura de NDVI vinda do analise-svc (collection historico_ndvi do MongoDB). */
public record NdviHistoricoResponse(
        String id,
        UUID plotId,
        UUID claimId,
        LocalDate imageDate,
        BigDecimal meanNdvi,
        BigDecimal meanEvi,
        String satelliteSource,
        BigDecimal cloudCoveragePct,
        OffsetDateTime createdAt
) {}
