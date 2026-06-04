package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByIdAndDeletedAtIsNull(UUID id);
    Page<Payment> findAllByDeletedAtIsNull(Pageable pageable);
    Page<Payment> findAllByClaimIdAndDeletedAtIsNull(UUID claimId, Pageable pageable);
}
