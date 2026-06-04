package com.fiap.zenith.core.infra.messaging;

import java.math.BigDecimal;
import java.util.UUID;

/** Evento publicado no RabbitMQ quando um sinistro é aberto. Consumido pelo analise-svc. */
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
