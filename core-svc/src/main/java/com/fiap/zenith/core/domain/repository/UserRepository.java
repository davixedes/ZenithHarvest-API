package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data para {@link User}. Consultas ignoram usuários com soft delete.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Page<User> findAllByDeletedAtIsNull(Pageable pageable);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByCpfAndDeletedAtIsNull(String cpf);
}
