package com.fiap.zenith.core.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CropResponse(
        UUID id,
        Integer code,
        String name,
        String scientificName,
        Integer averageCycleDays,
        BigDecimal expectedNdviMin,
        BigDecimal expectedNdviMax,
        BigDecimal averageValuePerHectare,
        BigDecimal droughtVulnerability,
        BigDecimal frostVulnerability,
        Boolean status,
        OffsetDateTime createdAt,
        OffsetDateTime editedAt
) {}
