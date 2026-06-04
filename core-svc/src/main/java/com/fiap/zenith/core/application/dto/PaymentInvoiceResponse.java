package com.fiap.zenith.core.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentInvoiceResponse(
        UUID id,
        Integer code,
        String invoiceNumber,
        UUID userId,
        UUID insurerId,
        BigDecimal totalAmount,
        LocalDate dueDate,
        OffsetDateTime issuedAt,
        OffsetDateTime paidAt,
        Boolean active
) {}
