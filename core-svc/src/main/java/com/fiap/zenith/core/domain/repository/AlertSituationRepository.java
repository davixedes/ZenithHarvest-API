package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.AlertSituation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertSituationRepository extends JpaRepository<AlertSituation, Integer> {
    List<AlertSituation> findAllByActiveTrue();
}
