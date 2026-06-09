package com.fiap.zenith.analise_svc.domain.repository;

import com.fiap.zenith.analise_svc.domain.entity.SatelliteAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SatelliteAnalysisRepository extends JpaRepository<SatelliteAnalysis, UUID> {
    List<SatelliteAnalysis> findAllByPlotIdAndDeletedAtIsNull(UUID plotId);
    List<SatelliteAnalysis> findAllByClaimIdAndDeletedAtIsNull(UUID claimId);

    /** Análise mais recente do talhão com imagem nos últimos N dias e cobertura de nuvens aceitável. */
    @Query("""
            select sa from SatelliteAnalysis sa
            where sa.plotId = :plotId
              and sa.deletedAt is null
              and sa.imageDate >= :limite
              and (sa.cloudCoveragePct is null or sa.cloudCoveragePct <= 80)
            order by sa.imageDate desc
            limit 1
            """)
    Optional<SatelliteAnalysis> findMaisRecenteParaVarredura(
            @Param("plotId") UUID plotId,
            @Param("limite") LocalDate limite);
}
