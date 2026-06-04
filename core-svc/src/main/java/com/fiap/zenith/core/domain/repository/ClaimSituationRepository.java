package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.ClaimSituation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimSituationRepository extends JpaRepository<ClaimSituation, Integer> {
    List<ClaimSituation> findAllByActiveTrue();
}
