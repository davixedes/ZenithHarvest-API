package com.fiap.zenith.core.application.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdatePolicyItemRequest(
        @Digits(integer = 3, fraction = 2) BigDecimal coveragePct,
        @Digits(integer = 13, fraction = 2) BigDecimal maxCoverageAmount,
        @Size(max = 500) String notes
) {}
