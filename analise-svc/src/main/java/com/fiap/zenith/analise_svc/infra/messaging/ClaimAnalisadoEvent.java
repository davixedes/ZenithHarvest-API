package com.fiap.zenith.analise_svc.infra.messaging;

import java.math.BigDecimal;
import java.util.UUID;

/** Resultado da análise publicado de volta para o core-svc. */
public record ClaimAnalisadoEvent(
        UUID claimId,
        BigDecimal ndviAfter,
        BigDecimal totalLossPct,
        BigDecimal totalAffectedAreaHa,
        BigDecimal calculatedAmount,
        BigDecimal mlConfidenceScore,
        Boolean fraudFlag,
        Integer newSituationId,
        String laudo
) {}
