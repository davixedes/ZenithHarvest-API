package com.fiap.zenith.core.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(
        @NotNull Integer paymentTypeId,
        @NotNull Integer paymentSituationId,
        UUID claimId,
        UUID policyId,
        UUID paymentInvoiceId,
        @Positive BigDecimal amount,
        @Size(max = 140) String pixKey
) {}
