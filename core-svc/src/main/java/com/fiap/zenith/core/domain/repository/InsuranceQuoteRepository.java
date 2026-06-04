package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.InsuranceQuote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InsuranceQuoteRepository extends JpaRepository<InsuranceQuote, UUID> {
    Optional<InsuranceQuote> findByIdAndDeletedAtIsNull(UUID id);
    Page<InsuranceQuote> findAllByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);
    Page<InsuranceQuote> findAllByDeletedAtIsNull(Pageable pageable);
}
