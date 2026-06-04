package com.fiap.zenith.core.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InsuranceResponse(
        UUID id,
        Integer code,
        UUID insurerId,
        String name,
        String description,
        BigDecimal deductiblePct,
        Integer graceDays,
        BigDecimal maxCoveragePerHectare,
        BigDecimal baseRatePct,
        String availableStates,
        Integer insuranceSituationId,
        Boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime editedAt
) {}
