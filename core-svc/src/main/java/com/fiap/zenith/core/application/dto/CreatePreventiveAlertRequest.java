package com.fiap.zenith.core.application.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePreventiveAlertRequest(
        @NotNull UUID plotId,
        @NotNull Integer alertTypeId,
        @NotNull Integer alertSeverityId,
        @NotNull Integer alertSituationId,
        @NotBlank String message,
        @Digits(integer = 1, fraction = 3) BigDecimal observedNdvi,
        @Digits(integer = 1, fraction = 3) BigDecimal expectedNdvi,
        @Digits(integer = 3, fraction = 2) BigDecimal dropPct
) {}
