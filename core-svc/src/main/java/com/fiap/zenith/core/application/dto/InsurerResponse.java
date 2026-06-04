package com.fiap.zenith.core.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InsurerResponse(
        UUID id,
        Integer code,
        String corporateName,
        String tradeName,
        String cnpj,
        String susepCode,
        String commercialEmail,
        String phone,
        String logoUrl,
        BigDecimal adminFeePct,
        BigDecimal takeRatePct,
        Boolean active,
        Integer insurerSituationId,
        LocalDate accreditedAt,
        OffsetDateTime createdAt,
        OffsetDateTime editedAt
) {}
