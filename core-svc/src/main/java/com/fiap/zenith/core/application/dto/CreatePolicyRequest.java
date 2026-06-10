package com.fiap.zenith.core.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePolicyRequest(
        UUID insuranceQuoteId,
        @NotNull UUID plotId,
        @NotNull UUID insurerId,
        @NotNull UUID insuranceId,
        @NotNull Integer policySituationId,
        @Positive BigDecimal insuredAmount,
        @Positive BigDecimal totalPremium,
        @Positive BigDecimal monthlyPremium,
        @Positive BigDecimal deductiblePct,
        @Positive BigDecimal maxCoverage,
        LocalDate startDate,
        LocalDate endDate
) {}
