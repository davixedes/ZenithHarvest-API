package com.fiap.zenith.analise_svc.infra.feign.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Resposta do core-svc após criar/consultar um PreventiveAlert. */
public record AlertaResponse(
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
