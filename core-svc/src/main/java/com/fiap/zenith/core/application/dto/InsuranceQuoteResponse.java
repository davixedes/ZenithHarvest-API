package com.fiap.zenith.core.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InsuranceQuoteResponse(
        UUID id,
        Integer code,
        UUID userId,
        UUID plotId,
        UUID insuranceId,
        Integer quoteSituationId,
        BigDecimal insuredAmount,
        BigDecimal totalPremium,
        BigDecimal monthlyPremium,
        BigDecimal regionalFactor,
        BigDecimal historyFactor,
        OffsetDateTime validUntil,
        OffsetDateTime acceptedAt,
        OffsetDateTime createdAt,
        OffsetDateTime editedAt
) {}
