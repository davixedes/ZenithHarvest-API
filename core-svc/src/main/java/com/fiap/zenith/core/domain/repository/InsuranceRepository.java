package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.Insurance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InsuranceRepository extends JpaRepository<Insurance, UUID> {
    Optional<Insurance> findByIdAndDeletedAtIsNull(UUID id);
    Page<Insurance> findAllByDeletedAtIsNull(Pageable pageable);
    boolean existsByInsurerIdAndNameAndDeletedAtIsNull(UUID insurerId, String name);
}
