package com.fiap.zenith.core.application.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateClaimItemRequest(
        @NotNull UUID claimId,
        @NotNull Integer claimEventTypeId,
        @Digits(integer = 8, fraction = 2) BigDecimal affectedAreaHa,
        @Digits(integer = 3, fraction = 2) BigDecimal lossPct,
        @Digits(integer = 1, fraction = 3) BigDecimal ndviBefore,
        @Digits(integer = 1, fraction = 3) BigDecimal ndviAfter,
        @Digits(integer = 13, fraction = 2) BigDecimal itemAmount,
        @Size(max = 500) String description
) {}
