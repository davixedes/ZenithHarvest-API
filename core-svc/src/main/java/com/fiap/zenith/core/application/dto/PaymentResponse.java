package com.fiap.zenith.core.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        Integer code,
        Integer paymentTypeId,
        Integer paymentSituationId,
        UUID claimId,
        UUID policyId,
        UUID paymentInvoiceId,
        BigDecimal amount,
        String pixKey,
        OffsetDateTime sentAt,
        OffsetDateTime confirmedAt,
        String pspTransactionId,
        Short attempts,
        String failureReason,
        OffsetDateTime createdAt,
        OffsetDateTime editedAt
) {}
