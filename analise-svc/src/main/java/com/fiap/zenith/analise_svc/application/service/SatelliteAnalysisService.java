package com.fiap.zenith.analise_svc.application.service;

import com.fiap.zenith.analise_svc.domain.document.HistoricoNdviDocument;
import com.fiap.zenith.analise_svc.domain.entity.SatelliteAnalysis;
import com.fiap.zenith.analise_svc.domain.repository.HistoricoNdviRepository;
import com.fiap.zenith.analise_svc.domain.repository.SatelliteAnalysisRepository;
import com.fiap.zenith.analise_svc.infra.messaging.ClaimAnalisadoEvent;
import com.fiap.zenith.analise_svc.infra.messaging.SinistroAbertoEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Random;

/**
 * Orquestra a análise satelital de um sinistro:
 * 1. Calcula NDVI simulado (em prod seria integração com Sentinel-2)
 * 2. Persiste SatelliteAnalysis no Postgres
 * 3. Grava histórico NDVI no MongoDB
 * 4. Dispara geração de laudo via Spring AI
 */
@Service
public class SatelliteAnalysisService {

    private static final int SATELLITE_SOURCE_SENTINEL2 = 1;
    private static final int SATELLITE_CLASS_ESTRESSE_SEVERO = 4;
    private static final int SATELLITE_CLASS_ESTRESSE_MODERADO = 3;
    private static final int SATELLITE_CLASS_ESTRESSE_LEVE = 2;
    private static final int SATELLITE_CLASS_SAUDAVEL = 1;

    // ClaimSituation (seed): 2=Em análise, 3=Aprovado
    private static final int SINISTRO_EM_ANALISE = 2;
    private static final int SINISTRO_APROVADO = 3;

    private final SatelliteAnalysisRepository satelliteAnalysisRepository;
    private final HistoricoNdviRepository historicoNdviRepository;
    private final LaudoService laudoService;

    public SatelliteAnalysisService(SatelliteAnalysisRepository satelliteAnalysisRepository,
                                     HistoricoNdviRepository historicoNdviRepository,
                                     LaudoService laudoService) {
        this.satelliteAnalysisRepository = satelliteAnalysisRepository;
        this.historicoNdviRepository = historicoNdviRepository;
        this.laudoService = laudoService;
    }

    /** Defaults usados apenas quando o evento não traz os dados (apólice/talhão incompletos). */
    private static final BigDecimal DEFAULT_NDVI_BEFORE = new BigDecimal("0.65");
    private static final BigDecimal DEFAULT_INSURED_AMOUNT = new BigDecimal("100000.00");
    private static final BigDecimal DEFAULT_PLOT_AREA_M2 = new BigDecimal("10000.00");

    @Transactional
    public ClaimAnalisadoEvent analisarSinistro(SinistroAbertoEvent evento) {
        BigDecimal ndviBefore = evento.ndviBefore() != null ? evento.ndviBefore() : DEFAULT_NDVI_BEFORE;
        BigDecimal insuredAmount = evento.insuredAmount() != null ? evento.insuredAmount() : DEFAULT_INSURED_AMOUNT;
        BigDecimal plotAreaM2 = evento.plotAreaM2() != null ? evento.plotAreaM2() : DEFAULT_PLOT_AREA_M2;

        BigDecimal ndviAfter = calcularNdviPosEvento(ndviBefore);
        BigDecimal totalLossPct = calcularPercentualPerda(ndviBefore, ndviAfter);
        BigDecimal mlConfidence = new BigDecimal("0.87");
        boolean fraudFlag = mlConfidence.compareTo(new BigDecimal("0.30")) < 0;
        int satelliteClassId = classificarNdvi(ndviAfter);

        // Área afetada proporcional à perda; indenização proporcional ao valor segurado real.
        BigDecimal lossFraction = totalLossPct.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal affectedAreaM2 = plotAreaM2.multiply(lossFraction).setScale(2, RoundingMode.HALF_UP);
        BigDecimal calculatedAmount = insuredAmount.multiply(lossFraction).setScale(2, RoundingMode.HALF_UP);

        SatelliteAnalysis analysis = SatelliteAnalysis.create(
                evento.claimId(), evento.plotId(), SATELLITE_SOURCE_SENTINEL2,
                satelliteClassId, LocalDate.now(), ndviAfter,
                ndviAfter.multiply(new BigDecimal("0.85")).setScale(3, RoundingMode.HALF_UP),
                new BigDecimal("5.00"), affectedAreaM2, mlConfidence);
        satelliteAnalysisRepository.save(analysis);

        historicoNdviRepository.save(HistoricoNdviDocument.create(
                evento.plotId(), evento.claimId(), LocalDate.now(),
                ndviAfter, ndviAfter.multiply(new BigDecimal("0.85")).setScale(3, RoundingMode.HALF_UP),
                "Sentinel-2", new BigDecimal("5.00")));

        String laudo = laudoService.gerarLaudo(evento, ndviAfter, totalLossPct, mlConfidence.multiply(new BigDecimal("100")));

        // Suspeita de fraude exige análise humana; senão aprova automaticamente.
        int newSituationId = fraudFlag ? SINISTRO_EM_ANALISE : SINISTRO_APROVADO;

        return new ClaimAnalisadoEvent(
                evento.claimId(), ndviAfter, totalLossPct,
                affectedAreaM2.divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP),
                calculatedAmount, mlConfidence.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP),
                fraudFlag, newSituationId, laudo);
    }

    /** Simula NDVI pós-evento: redução entre 20-60% do valor original. */
    private BigDecimal calcularNdviPosEvento(BigDecimal ndviBefore) {
        double reducao = 0.20 + (0.40 * Math.random());
        double ndviAfter = ndviBefore.doubleValue() * (1 - reducao);
        return BigDecimal.valueOf(Math.max(-1.0, ndviAfter)).setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularPercentualPerda(BigDecimal ndviBefore, BigDecimal ndviAfter) {
        if (ndviBefore.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        BigDecimal queda = ndviBefore.subtract(ndviAfter);
        BigDecimal pct = queda.divide(ndviBefore, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        return pct.max(BigDecimal.ZERO).min(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    private int classificarNdvi(BigDecimal ndvi) {
        double v = ndvi.doubleValue();
        if (v >= 0.6) return SATELLITE_CLASS_SAUDAVEL;
        if (v >= 0.4) return SATELLITE_CLASS_ESTRESSE_LEVE;
        if (v >= 0.2) return SATELLITE_CLASS_ESTRESSE_MODERADO;
        return SATELLITE_CLASS_ESTRESSE_SEVERO;
    }
}
