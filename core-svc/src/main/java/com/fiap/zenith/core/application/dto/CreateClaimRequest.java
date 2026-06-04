package com.fiap.zenith.core.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateClaimRequest(
        @NotBlank @Size(max = 30) String claimNumber,
        @NotNull UUID policyId,
        @NotNull Integer claimSituationId,
        @NotNull Integer categoryId,
        @NotNull Integer subCategoryId,
        String description,
        @Size(max = 500) String photoUrl,
        BigDecimal openingGpsLat,
        BigDecimal openingGpsLng,
        BigDecimal ndviBefore
) {}
