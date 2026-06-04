package com.fiap.zenith.analise_svc.domain.repository;

import com.fiap.zenith.analise_svc.domain.entity.SatelliteAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SatelliteAnalysisRepository extends JpaRepository<SatelliteAnalysis, UUID> {
    List<SatelliteAnalysis> findAllByPlotIdAndDeletedAtIsNull(UUID plotId);
    List<SatelliteAnalysis> findAllByClaimIdAndDeletedAtIsNull(UUID claimId);
}
