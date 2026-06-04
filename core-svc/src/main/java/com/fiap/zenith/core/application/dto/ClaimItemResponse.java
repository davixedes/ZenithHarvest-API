package com.fiap.zenith.core.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ClaimItemResponse(
        UUID id,
        UUID claimId,
        Integer claimEventTypeId,
        BigDecimal affectedAreaHa,
        BigDecimal lossPct,
        BigDecimal ndviBefore,
        BigDecimal ndviAfter,
        BigDecimal itemAmount,
        String description
) {}
