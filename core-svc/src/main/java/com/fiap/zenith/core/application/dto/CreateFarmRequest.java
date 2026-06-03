package com.fiap.zenith.core.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payload de criação de uma fazenda. Validado com Bean Validation na fronteira da API.
 * {@code biomeId}, {@code propertyType} e {@code polygonWkt} são opcionais (operacionais).
 */
public record CreateFarmRequest(

        @NotNull(message = "userId é obrigatório")
        UUID userId,

        @NotBlank(message = "name é obrigatório")
        @Size(max = 150, message = "name deve ter no máximo 150 caracteres")
        String name,

        @NotBlank(message = "carRegistration é obrigatório")
        @Size(max = 50, message = "carRegistration deve ter no máximo 50 caracteres")
        String carRegistration,

        @NotBlank(message = "nirf é obrigatório")
        @Size(max = 20, message = "nirf deve ter no máximo 20 caracteres")
        String nirf,

        @NotNull(message = "latitude é obrigatória")
        @DecimalMin(value = "-90.0", message = "latitude deve estar entre -90 e 90")
        @DecimalMax(value = "90.0", message = "latitude deve estar entre -90 e 90")
        @Digits(integer = 3, fraction = 7, message = "latitude deve ter no máximo 7 casas decimais")
        BigDecimal latitude,

        @NotNull(message = "longitude é obrigatória")
        @DecimalMin(value = "-180.0", message = "longitude deve estar entre -180 e 180")
        @DecimalMax(value = "180.0", message = "longitude deve estar entre -180 e 180")
        @Digits(integer = 3, fraction = 7, message = "longitude deve ter no máximo 7 casas decimais")
        BigDecimal longitude,

        @NotNull(message = "totalAreaHectares é obrigatória")
        @Positive(message = "totalAreaHectares deve ser positiva")
        @Digits(integer = 8, fraction = 2, message = "totalAreaHectares inválida")
        BigDecimal totalAreaHectares,

        @NotBlank(message = "state é obrigatório")
        @Pattern(regexp = "[A-Z]{2}", message = "state deve ser a UF em 2 letras maiúsculas (ex: SP)")
        String state,

        Integer biomeId,

        @Size(max = 30, message = "propertyType deve ter no máximo 30 caracteres")
        String propertyType,

        String polygonWkt

) {
}
