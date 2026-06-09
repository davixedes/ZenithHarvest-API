package com.fiap.zenith.analise_svc.infra.feign.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payload enviado ao core-svc para criar um PreventiveAlert.
 * Espelha CreatePreventiveAlertRequest do core-svc.
 */
public record CriarAlertaRequest(
        UUID plotId,
        Integer alertTypeId,
        Integer alertSeverityId,
        Integer alertSituationId,
        String message,
        BigDecimal observedNdvi,
        BigDecimal expectedNdvi,
        BigDecimal dropPct
) {}
