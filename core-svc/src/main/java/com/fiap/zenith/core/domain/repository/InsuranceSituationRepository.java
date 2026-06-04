package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.InsuranceSituation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InsuranceSituationRepository extends JpaRepository<InsuranceSituation, Integer> {
    List<InsuranceSituation> findAllByActiveTrue();
}
