package com.fiap.zenith.core.application.dto.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload de cadastro de um novo usuário (produtor). O {@code address} é opcional.
 */
public record RegisterRequest(

        @NotBlank(message = "cpf é obrigatório")
        @Size(max = 18)
        String cpf,

        @NotBlank(message = "name é obrigatório")
        @Size(max = 150)
        String name,

        @NotBlank(message = "lastName é obrigatório")
        @Size(max = 150)
        String lastName,

        @NotBlank(message = "email é obrigatório")
        @Email(message = "email inválido")
        @Size(max = 150)
        String email,

        @NotBlank(message = "phone é obrigatório")
        @Size(max = 20)
        String phone,

        @NotBlank(message = "password é obrigatório")
        @Size(min = 8, max = 100, message = "password deve ter entre 8 e 100 caracteres")
        String password,

        @Valid
        AddressRequest address

) {
}
