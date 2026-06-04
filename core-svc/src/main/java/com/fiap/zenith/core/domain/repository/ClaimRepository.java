package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.Claim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {
    Optional<Claim> findByIdAndDeletedAtIsNull(UUID id);
    Page<Claim> findAllByPolicyIdAndDeletedAtIsNull(UUID policyId, Pageable pageable);
    Page<Claim> findAllByDeletedAtIsNull(Pageable pageable);
}
