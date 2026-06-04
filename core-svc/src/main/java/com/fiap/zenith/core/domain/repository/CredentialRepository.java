package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data para {@link Credential}. Busca pela credencial ativa de um usuário.
 */
public interface CredentialRepository extends JpaRepository<Credential, UUID> {

    Optional<Credential> findByUserIdAndDeletedAtIsNull(UUID userId);
}
