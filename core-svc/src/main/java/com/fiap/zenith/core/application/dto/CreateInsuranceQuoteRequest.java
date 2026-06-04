package com.fiap.zenith.core.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateInsuranceQuoteRequest(
        @NotNull UUID userId,
        @NotNull UUID plotId,
        @NotNull UUID insuranceId,
        @NotNull Integer quoteSituationId,
        @Positive BigDecimal insuredAmount,
        @Positive BigDecimal totalPremium,
        @Positive BigDecimal monthlyPremium,
        BigDecimal regionalFactor,
        BigDecimal historyFactor,
        OffsetDateTime validUntil
) {}
