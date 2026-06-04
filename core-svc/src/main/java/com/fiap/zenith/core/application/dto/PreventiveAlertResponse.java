package com.fiap.zenith.core.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PreventiveAlertResponse(
        UUID id,
        UUID plotId,
        Integer alertTypeId,
        Integer alertSeverityId,
        Integer alertSituationId,
        String message,
        BigDecimal observedNdvi,
        BigDecimal expectedNdvi,
        BigDecimal dropPct,
        OffsetDateTime issuedAt,
        OffsetDateTime viewedAt,
        OffsetDateTime createdAt
) {}
