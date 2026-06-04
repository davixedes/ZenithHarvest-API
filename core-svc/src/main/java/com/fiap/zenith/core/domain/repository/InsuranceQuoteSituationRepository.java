package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.InsuranceQuoteSituation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InsuranceQuoteSituationRepository extends JpaRepository<InsuranceQuoteSituation, Integer> {
    List<InsuranceQuoteSituation> findAllByActiveTrue();
}
