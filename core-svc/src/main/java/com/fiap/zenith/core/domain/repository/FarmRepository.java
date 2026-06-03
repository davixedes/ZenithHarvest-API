package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.Farm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data para {@link Farm}. Todas as consultas ignoram linhas com soft delete
 * ({@code DeletedAt IS NOT NULL}) — uma fazenda "removida" some das listagens/buscas.
 */
public interface FarmRepository extends JpaRepository<Farm, UUID> {

    Optional<Farm> findByIdAndDeletedAtIsNull(UUID id);

    Page<Farm> findAllByDeletedAtIsNull(Pageable pageable);

    boolean existsByCarRegistrationAndDeletedAtIsNull(String carRegistration);
}
