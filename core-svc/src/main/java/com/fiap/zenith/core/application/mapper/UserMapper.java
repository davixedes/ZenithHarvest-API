package com.fiap.zenith.core.application.mapper;

import com.fiap.zenith.core.application.dto.UserResponse;
import com.fiap.zenith.core.domain.entity.User;
import org.springframework.stereotype.Component;

/**
 * Converte a entidade {@link User} no DTO de saída {@link UserResponse} (nunca expõe
 * senha/secret, que vivem em Credential). Concentra o acoplamento entity↔DTO.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getCode(),
                user.getCpf(),
                user.getName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddressId(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }
}
