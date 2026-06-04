package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.Crop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CropRepository extends JpaRepository<Crop, UUID> {
    Optional<Crop> findByIdAndDeletedAtIsNull(UUID id);
    Page<Crop> findAllByDeletedAtIsNull(Pageable pageable);
    boolean existsByNameAndDeletedAtIsNull(String name);
}
