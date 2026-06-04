package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.PlotSituation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlotSituationRepository extends JpaRepository<PlotSituation, Integer> {
    List<PlotSituation> findAllByActiveTrue();
}
