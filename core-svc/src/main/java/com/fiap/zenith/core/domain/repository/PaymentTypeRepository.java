package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentTypeRepository extends JpaRepository<PaymentType, Integer> {
    List<PaymentType> findAllByActiveTrue();
}
