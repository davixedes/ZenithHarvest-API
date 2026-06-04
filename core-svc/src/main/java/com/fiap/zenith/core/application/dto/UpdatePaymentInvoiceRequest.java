package com.fiap.zenith.core.application.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdatePaymentInvoiceRequest(
        UUID insurerId,
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal totalAmount,
        @NotNull LocalDate dueDate
) {}
