package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.PaymentInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentInvoiceRepository extends JpaRepository<PaymentInvoice, UUID> {
    Optional<PaymentInvoice> findByIdAndActiveTrue(UUID id);
    Page<PaymentInvoice> findAllByActiveTrue(Pageable pageable);
    Page<PaymentInvoice> findAllByUserIdAndActiveTrue(UUID userId, Pageable pageable);
    boolean existsByInvoiceNumber(String invoiceNumber);
}
