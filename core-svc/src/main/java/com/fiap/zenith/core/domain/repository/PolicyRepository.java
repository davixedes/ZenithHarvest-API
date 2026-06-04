package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.Policy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PolicyRepository extends JpaRepository<Policy, UUID> {
    Optional<Policy> findByIdAndDeletedAtIsNull(UUID id);
    Page<Policy> findAllByDeletedAtIsNull(Pageable pageable);
    boolean existsByPolicyNumberAndDeletedAtIsNull(String policyNumber);
}
