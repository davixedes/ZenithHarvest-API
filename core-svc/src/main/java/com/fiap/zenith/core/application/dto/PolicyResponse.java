package com.fiap.zenith.core.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PolicyResponse(
        UUID id,
        Integer code,
        String policyNumber,
        UUID insuranceQuoteId,
        UUID plotId,
        UUID insurerId,
        UUID insuranceId,
        Integer policySituationId,
        BigDecimal insuredAmount,
        BigDecimal totalPremium,
        BigDecimal monthlyPremium,
        BigDecimal deductiblePct,
        BigDecimal maxCoverage,
        BigDecimal accumulatedPaid,
        LocalDate startDate,
        LocalDate endDate,
        OffsetDateTime contractedAt,
        OffsetDateTime cancelledAt,
        OffsetDateTime createdAt,
        OffsetDateTime editedAt
) {}
