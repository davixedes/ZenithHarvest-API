package com.fiap.zenith.core.application.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePaymentInvoiceRequest(
        @NotBlank @Size(max = 30) String invoiceNumber,
        @NotNull UUID userId,
        UUID insurerId,
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal totalAmount,
        @NotNull LocalDate dueDate
) {}
