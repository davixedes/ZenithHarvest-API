package com.fiap.zenith.core.infra.messaging;

import java.math.BigDecimal;
import java.util.UUID;

/** Evento recebido do analise-svc com o resultado da análise de satélite e IA. */
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
