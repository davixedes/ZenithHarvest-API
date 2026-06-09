package com.fiap.zenith.analise_svc.application.service;

import com.fiap.zenith.analise_svc.domain.entity.CropInfo;
import com.fiap.zenith.analise_svc.domain.entity.PlotInfo;
import com.fiap.zenith.analise_svc.domain.entity.SatelliteAnalysis;
import com.fiap.zenith.analise_svc.domain.repository.CropInfoRepository;
import com.fiap.zenith.analise_svc.domain.repository.PlotInfoRepository;
import com.fiap.zenith.analise_svc.domain.repository.PolicyInfoRepository;
import com.fiap.zenith.analise_svc.domain.repository.SatelliteAnalysisRepository;
import com.fiap.zenith.analise_svc.infra.feign.CoreSvcAlertaClient;
import com.fiap.zenith.analise_svc.infra.feign.dto.CriarAlertaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Varredura periódica de NDVI: compara o índice observado com o limiar da cultura e
 * emite PreventiveAlert via Feign ao core-svc quando detecta queda anormal.
 */
@Service
public class AlertaDeteccaoService {

    private static final Logger log = LoggerFactory.getLogger(AlertaDeteccaoService.class);

    // Limiares de severidade (% de queda abaixo do expectedNdviMin)
    private static final double LIMIAR_INFORMATIVO = 15.0;
    private static final double LIMIAR_ATENCAO = 30.0;
    private static final double LIMIAR_CRITICO = 50.0;

    // IDs de alerta conforme seed (AlertType e AlertSeverity, inserção em ordem)
    private static final int ALERT_TYPE_QUEDA_NDVI = 1;
    private static final int SEVERIDADE_INFORMATIVO = 1;
    private static final int SEVERIDADE_ATENCAO = 2;
    private static final int SEVERIDADE_CRITICO = 3;
    private static final int SITUACAO_ABERTO = 1;

    // Janela máxima de idade da imagem: 7 dias
    private static final int JANELA_DIAS = 7;

    private final PolicyInfoRepository policyInfoRepository;
    private final PlotInfoRepository plotInfoRepository;
    private final CropInfoRepository cropInfoRepository;
    private final SatelliteAnalysisRepository analysisRepository;
    private final AlertaMensagemService mensagemService;
    private final CoreSvcAlertaClient alertaClient;

    public AlertaDeteccaoService(PolicyInfoRepository policyInfoRepository,
                                  PlotInfoRepository plotInfoRepository,
                                  CropInfoRepository cropInfoRepository,
                                  SatelliteAnalysisRepository analysisRepository,
                                  AlertaMensagemService mensagemService,
                                  CoreSvcAlertaClient alertaClient) {
        this.policyInfoRepository = policyInfoRepository;
        this.plotInfoRepository = plotInfoRepository;
        this.cropInfoRepository = cropInfoRepository;
        this.analysisRepository = analysisRepository;
        this.mensagemService = mensagemService;
        this.alertaClient = alertaClient;
    }

    public void executarVarredura() {
        LocalDate hoje = LocalDate.now();
        List<UUID> plotIds = policyInfoRepository.findPlotIdsComApoliceVigente(hoje);

        log.info("Varredura de alertas iniciada — {} talhões com apólice vigente", plotIds.size());

        int emitidos = 0;
        for (UUID plotId : plotIds) {
            try {
                if (processarPlot(plotId, hoje)) emitidos++;
            } catch (Exception e) {
                log.error("Erro ao processar plot {} na varredura: {}", plotId, e.getMessage());
            }
        }

        log.info("Varredura concluída — {} alertas emitidos de {} talhões", emitidos, plotIds.size());
    }

    /**
     * Retorna true se um alerta foi emitido para este plot.
     */
    private boolean processarPlot(UUID plotId, LocalDate hoje) {
        // 1. Buscar análise mais recente (últimos 7 dias, nuvens <= 80%)
        Optional<SatelliteAnalysis> analiseOpt = analysisRepository
                .findMaisRecenteParaVarredura(plotId, hoje.minusDays(JANELA_DIAS));
        if (analiseOpt.isEmpty()) {
            log.debug("Plot {}: sem análise recente (últimos {} dias)", plotId, JANELA_DIAS);
            return false;
        }

        SatelliteAnalysis analise = analiseOpt.get();
        BigDecimal ndviObservado = analise.getMeanNdvi();
        if (ndviObservado == null) return false;

        // 2. Buscar talhão e cultura
        Optional<PlotInfo> plotOpt = plotInfoRepository.findByIdAndDeletedAtIsNull(plotId);
        if (plotOpt.isEmpty() || plotOpt.get().getCropId() == null) return false;

        Optional<CropInfo> cropOpt = cropInfoRepository.findByIdAndDeletedAtIsNull(plotOpt.get().getCropId());
        if (cropOpt.isEmpty() || cropOpt.get().getExpectedNdviMin() == null) return false;

        CropInfo crop = cropOpt.get();
        BigDecimal ndviEsperado = crop.getExpectedNdviMin();

        // 3. Calcular queda percentual
        if (ndviObservado.compareTo(ndviEsperado) >= 0) return false; // sem queda

        BigDecimal dropPct = ndviEsperado.subtract(ndviObservado)
                .divide(ndviEsperado, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        double drop = dropPct.doubleValue();
        if (drop < LIMIAR_INFORMATIVO) return false; // ruído tolerável

        // 4. Determinar severidade
        int severidadeId = determinarSeveridade(drop);
        String severidadeDescricao = descricaoSeveridade(severidadeId);

        // 5. Gerar mensagem (Spring AI com fallback)
        String mensagem = mensagemService.gerarMensagem(
                crop.getName(), ndviObservado, ndviEsperado, dropPct, severidadeDescricao);

        // 6. Enviar ao core-svc (anti-duplicidade é feita lá)
        CriarAlertaRequest req = new CriarAlertaRequest(
                plotId, ALERT_TYPE_QUEDA_NDVI, severidadeId, SITUACAO_ABERTO,
                mensagem, ndviObservado, ndviEsperado, dropPct);

        alertaClient.criarAlerta(req);

        log.info("Plot {}: alerta {} emitido (NDVI={}, queda={}%)",
                plotId, severidadeDescricao, ndviObservado.toPlainString(), drop);
        return true;
    }

    private int determinarSeveridade(double dropPct) {
        if (dropPct >= LIMIAR_CRITICO) return SEVERIDADE_CRITICO;
        if (dropPct >= LIMIAR_ATENCAO) return SEVERIDADE_ATENCAO;
        return SEVERIDADE_INFORMATIVO;
    }

    private String descricaoSeveridade(int id) {
        return switch (id) {
            case SEVERIDADE_CRITICO -> "Crítico";
            case SEVERIDADE_ATENCAO -> "Atenção";
            default -> "Informativo";
        };
    }
}
