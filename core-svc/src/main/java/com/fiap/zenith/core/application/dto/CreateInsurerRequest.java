package com.fiap.zenith.core.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateInsurerRequest(
        @NotBlank @Size(max = 200) String corporateName,
        @Size(max = 150) String tradeName,
        @NotBlank @Size(max = 18) String cnpj,
        @Size(max = 20) String susepCode,
        @Email @Size(max = 150) String commercialEmail,
        @Size(max = 20) String phone,
        @Size(max = 500) String logoUrl,
        @Positive BigDecimal adminFeePct,
        @Positive BigDecimal takeRatePct,
        @NotNull Integer insurerSituationId,
        LocalDate accreditedAt
) {}
