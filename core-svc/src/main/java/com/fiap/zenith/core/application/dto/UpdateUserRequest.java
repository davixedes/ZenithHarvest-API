package com.fiap.zenith.core.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Atualização do perfil do usuário. CPF, e-mail e Code são imutáveis após o cadastro.
 */
public record UpdateUserRequest(

        @NotBlank(message = "name é obrigatório")
        @Size(max = 150)
        String name,

        @NotBlank(message = "lastName é obrigatório")
        @Size(max = 150)
        String lastName,

        @NotBlank(message = "phone é obrigatório")
        @Size(max = 20)
        String phone

) {
}
