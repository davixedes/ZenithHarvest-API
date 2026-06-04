package com.fiap.zenith.core.application.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Endereço opcional enviado no cadastro do usuário.
 */
public record AddressRequest(

        @NotBlank @Size(max = 150) String street,
        @NotNull @Positive Integer number,
        @NotBlank @Size(max = 100) String neighboor,
        @NotBlank @Size(max = 100) String city,
        @Size(max = 150) String complement,
        @NotBlank @Size(max = 9) String postalCode,
        @NotBlank @Pattern(regexp = "[A-Z]{2}", message = "uf deve ser a sigla em 2 letras maiúsculas") String uf,
        @NotBlank @Size(max = 60) String country

) {
}
