package com.fiap.zenith.core.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateInsuranceRequest(
        @NotNull UUID insurerId,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        @Positive BigDecimal deductiblePct,
        @Positive Integer graceDays,
        @Positive BigDecimal maxCoveragePerHectare,
        @Positive BigDecimal baseRatePct,
        @Size(max = 100) String availableStates,
        @NotNull Integer insuranceSituationId
) {}
