package com.fiap.zenith.analise_svc.web.controller;

import com.fiap.zenith.analise_svc.application.dto.SatelliteAnalysisResponse;
import com.fiap.zenith.analise_svc.domain.document.HistoricoNdviDocument;
import com.fiap.zenith.analise_svc.domain.repository.HistoricoNdviRepository;
import com.fiap.zenith.analise_svc.domain.repository.SatelliteAnalysisRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ndvi")
@Tag(name = "NDVI", description = "Histórico e análises satelitais de talhões")
public class NdviController {

    private final HistoricoNdviRepository historicoNdviRepository;
    private final SatelliteAnalysisRepository satelliteAnalysisRepository;

    public NdviController(HistoricoNdviRepository historicoNdviRepository,
                           SatelliteAnalysisRepository satelliteAnalysisRepository) {
        this.historicoNdviRepository = historicoNdviRepository;
        this.satelliteAnalysisRepository = satelliteAnalysisRepository;
    }

    @GetMapping("/{plotId}/historico")
    @Operation(summary = "Histórico de NDVI do talhão (MongoDB — série temporal)")
    public List<HistoricoNdviDocument> historico(@PathVariable UUID plotId) {
        return historicoNdviRepository.findAllByPlotIdOrderByImageDateDesc(plotId);
    }

    @GetMapping("/{plotId}/analises")
    @Operation(summary = "Análises satelitais do talhão (Postgres)")
    public List<SatelliteAnalysisResponse> analises(@PathVariable UUID plotId) {
        return satelliteAnalysisRepository.findAllByPlotIdAndDeletedAtIsNull(plotId).stream()
                .map(sa -> new SatelliteAnalysisResponse(
                        sa.getId(), sa.getClaimId(), sa.getPlotId(),
                        sa.getSatelliteSourceId(), sa.getSatelliteClassId(),
                        sa.getImageDate(), sa.getMeanNdvi(), sa.getMeanEvi(),
                        sa.getCloudCoveragePct(), sa.getAffectedAreaM2(),
                        sa.getMlConfidence(), sa.getProcessedAt(), sa.getCreatedAt()))
                .toList();
    }
}
