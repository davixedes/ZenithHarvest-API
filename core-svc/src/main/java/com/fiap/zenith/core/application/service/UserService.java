package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.UpdateUserRequest;
import com.fiap.zenith.core.application.dto.UserResponse;
import com.fiap.zenith.core.application.mapper.UserMapper;
import com.fiap.zenith.core.domain.entity.User;
import com.fiap.zenith.core.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * CRUD do perfil de {@link User}. O cadastro (com senha) fica no {@link AuthService};
 * aqui ficam consulta, atualização de perfil e remoção lógica.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public UserResponse buscarPorId(UUID id) {
        return userMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> listar(Pageable pageable) {
        return userRepository.findAllByDeletedAtIsNull(pageable).map(userMapper::toResponse);
    }

    @Transactional
    public UserResponse atualizar(UUID id, UpdateUserRequest req) {
        User user = buscarEntidade(id);
        user.setName(req.name());
        user.setLastName(req.lastName());
        user.setPhone(req.phone());
        OffsetDateTime now = OffsetDateTime.now();
        user.setEditedAt(now);
        user.setUpdatedAt(now);
        return userMapper.toResponse(user);
    }

    @Transactional
    public void remover(UUID id) {
        User user = buscarEntidade(id);
        user.setDeletedAt(OffsetDateTime.now());
    }

    private User buscarEntidade(UUID id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));
    }
}
