package com.fiap.zenith.core.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ClaimResponse(
        UUID id,
        Integer code,
        String claimNumber,
        UUID policyId,
        Integer claimSituationId,
        Integer categoryId,
        Integer subCategoryId,
        String description,
        String photoUrl,
        BigDecimal openingGpsLat,
        BigDecimal openingGpsLng,
        BigDecimal ndviBefore,
        BigDecimal ndviAfter,
        BigDecimal totalLossPct,
        BigDecimal totalAffectedAreaHa,
        BigDecimal calculatedAmount,
        BigDecimal approvedAmount,
        BigDecimal mlConfidenceScore,
        Boolean fraudFlag,
        Integer rejectionReasonId,
        OffsetDateTime approvedAt,
        OffsetDateTime paidAt,
        OffsetDateTime createdAt,
        OffsetDateTime editedAt
) {}
