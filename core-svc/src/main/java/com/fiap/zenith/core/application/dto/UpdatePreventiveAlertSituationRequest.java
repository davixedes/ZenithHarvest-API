package com.fiap.zenith.core.application.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePreventiveAlertSituationRequest(
        @NotNull Integer alertSituationId
) {}
