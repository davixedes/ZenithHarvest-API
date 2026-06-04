package com.fiap.zenith.core.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PolicyItemResponse(
        UUID id,
        UUID policyId,
        Integer claimEventTypeId,
        BigDecimal coveragePct,
        BigDecimal maxCoverageAmount,
        String notes
) {}
