package com.fiap.zenith.core.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateCropRequest(
        @NotBlank @Size(max = 80) String name,
        @Size(max = 120) String scientificName,
        @Positive Integer averageCycleDays,
        @DecimalMin("-1.000") @DecimalMax("1.000") BigDecimal expectedNdviMin,
        @DecimalMin("-1.000") @DecimalMax("1.000") BigDecimal expectedNdviMax,
        @Positive BigDecimal averageValuePerHectare,
        @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal droughtVulnerability,
        @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal frostVulnerability
) {}
