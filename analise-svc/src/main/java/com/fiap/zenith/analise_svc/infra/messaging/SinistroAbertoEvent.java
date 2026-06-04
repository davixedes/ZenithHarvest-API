package com.fiap.zenith.analise_svc.infra.messaging;

import java.math.BigDecimal;
import java.util.UUID;

/** Evento recebido do core-svc quando um sinistro é aberto. */
public record SinistroAbertoEvent(
        UUID claimId,
        String claimNumber,
        UUID policyId,
        UUID plotId,
        Integer categoryId,
        Integer subCategoryId,
        BigDecimal ndviBefore,
        BigDecimal openingGpsLat,
        BigDecimal openingGpsLng,
        String description
) {}
