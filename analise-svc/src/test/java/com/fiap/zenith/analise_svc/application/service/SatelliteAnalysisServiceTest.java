package com.fiap.zenith.analise_svc.application.service;

import com.fiap.zenith.analise_svc.domain.document.HistoricoNdviDocument;
import com.fiap.zenith.analise_svc.domain.entity.SatelliteAnalysis;
import com.fiap.zenith.analise_svc.domain.repository.HistoricoNdviRepository;
import com.fiap.zenith.analise_svc.domain.repository.SatelliteAnalysisRepository;
import com.fiap.zenith.analise_svc.infra.messaging.ClaimAnalisadoEvent;
import com.fiap.zenith.analise_svc.infra.messaging.SinistroAbertoEvent;
import com.fiap.zenith.analise_svc.infra.satellite.SentinelHubObservation;
import com.fiap.zenith.analise_svc.infra.satellite.SentinelHubService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SatelliteAnalysisServiceTest {

    @Mock
    private SatelliteAnalysisRepository satelliteAnalysisRepository;

    @Mock
    private HistoricoNdviRepository historicoNdviRepository;

    @Mock
    private LaudoService laudoService;

    @Mock
    private SentinelHubService sentinelHubService;

    @InjectMocks
    private SatelliteAnalysisService service;

    @Test
    void analisarSinistroDeveUsarNdviRealQuandoSentinelRetornarObservacao() {
        SinistroAbertoEvent evento = evento();
        when(sentinelHubService.buscarNdvi(evento))
                .thenReturn(Optional.of(new SentinelHubObservation(new BigDecimal("0.412"), new BigDecimal("12.30"))));
        when(laudoService.gerarLaudo(any(), any(), any(), any())).thenReturn("laudo");

        ClaimAnalisadoEvent resultado = service.analisarSinistro(evento);

        ArgumentCaptor<SatelliteAnalysis> satelliteCaptor = ArgumentCaptor.forClass(SatelliteAnalysis.class);
        verify(satelliteAnalysisRepository).save(satelliteCaptor.capture());
        assertThat(satelliteCaptor.getValue().getMeanNdvi()).isEqualByComparingTo("0.412");
        assertThat(satelliteCaptor.getValue().getCloudCoveragePct()).isEqualByComparingTo("12.30");

        ArgumentCaptor<HistoricoNdviDocument> historicoCaptor = ArgumentCaptor.forClass(HistoricoNdviDocument.class);
        verify(historicoNdviRepository).save(historicoCaptor.capture());
        assertThat(historicoCaptor.getValue().getMeanNdvi()).isEqualByComparingTo("0.412");

        assertThat(resultado.ndviAfter()).isEqualByComparingTo("0.412");
        assertThat(resultado.laudo()).isEqualTo("laudo");
    }

    @Test
    void analisarSinistroDeveUsarFallbackQuandoSentinelNaoEstiverDisponivel() {
        SinistroAbertoEvent evento = evento();
        when(sentinelHubService.buscarNdvi(evento)).thenReturn(Optional.empty());
        when(laudoService.gerarLaudo(any(), any(), any(), any())).thenReturn("laudo");

        ClaimAnalisadoEvent resultado = service.analisarSinistro(evento);

        assertThat(resultado.ndviAfter()).isBetween(new BigDecimal("0.240"), new BigDecimal("0.480"));
        assertThat(resultado.totalLossPct()).isBetween(new BigDecimal("20.00"), new BigDecimal("60.00"));
    }

    private SinistroAbertoEvent evento() {
        return new SinistroAbertoEvent(
                UUID.randomUUID(),
                "CLM-2026-0001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                1,
                2,
                new BigDecimal("0.600"),
                new BigDecimal("-22.9100"),
                new BigDecimal("-47.0650"),
                "Queda de vigor",
                new BigDecimal("120000.00"),
                new BigDecimal("40000.00")
        );
    }
}
