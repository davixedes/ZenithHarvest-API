package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.Plot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlotRepository extends JpaRepository<Plot, UUID> {
    Optional<Plot> findByIdAndDeletedAtIsNull(UUID id);
    Page<Plot> findAllByFarmIdAndDeletedAtIsNull(UUID farmId, Pageable pageable);
    Page<Plot> findAllByDeletedAtIsNull(Pageable pageable);
    boolean existsByFarmIdAndIdentifierAndDeletedAtIsNull(UUID farmId, String identifier);
}
