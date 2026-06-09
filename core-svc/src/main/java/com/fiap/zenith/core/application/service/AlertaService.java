package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.CreatePreventiveAlertRequest;
import com.fiap.zenith.core.application.dto.PreventiveAlertResponse;
import com.fiap.zenith.core.domain.entity.PreventiveAlert;
import com.fiap.zenith.core.domain.repository.PreventiveAlertRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AlertaService {

    // IDs de situação conforme seed (AlertSituation, inserção em ordem)
    private static final int SITUACAO_ABERTO = 1;
    private static final int SITUACAO_VISUALIZADO = 2;
    private static final int SITUACAO_RESOLVIDO = 3;
    private static final int SITUACAO_DESCARTADO = 4;

    private static final List<Integer> SITUACOES_TERMINAIS = List.of(SITUACAO_RESOLVIDO, SITUACAO_DESCARTADO);
    private static final List<Integer> SITUACOES_ABERTOS = List.of(SITUACAO_ABERTO, SITUACAO_VISUALIZADO);

    private final PreventiveAlertRepository alertaRepository;

    public AlertaService(PreventiveAlertRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    @Transactional
    public PreventiveAlertResponse criar(CreatePreventiveAlertRequest req) {
        // Anti-duplicidade: nunca dois alertas ativos do mesmo tipo para o mesmo plot
        if (alertaRepository.existsByPlotIdAndAlertTypeIdAndAlertSituationIdIn(
                req.plotId(), req.alertTypeId(), SITUACOES_ABERTOS)) {
            // Retorna silenciosamente — o analise-svc não precisa saber que foi ignorado
            return buscarAtivoPorPlotETipo(req.plotId(), req.alertTypeId());
        }

        PreventiveAlert alerta = PreventiveAlert.create(
                req.plotId(), req.alertTypeId(), req.alertSeverityId(),
                SITUACAO_ABERTO, req.message(),
                req.observedNdvi(), req.expectedNdvi(), req.dropPct());

        return toResponse(alertaRepository.save(alerta));
    }

    @Transactional(readOnly = true)
    public Page<PreventiveAlertResponse> listar(Pageable pageable) {
        return alertaRepository.findAllWithActivePlot(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PreventiveAlertResponse> listarPorPlot(UUID plotId, Pageable pageable) {
        return alertaRepository.findAllByPlotIdWithActivePlot(plotId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PreventiveAlertResponse buscarPorId(UUID id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public PreventiveAlertResponse visualizar(UUID id) {
        PreventiveAlert alerta = buscarEntidade(id);
        validarNaoTerminal(alerta);
        alerta.setAlertSituationId(SITUACAO_VISUALIZADO);
        alerta.setViewedAt(OffsetDateTime.now());
        return toResponse(alerta);
    }

    @Transactional
    public PreventiveAlertResponse resolver(UUID id) {
        PreventiveAlert alerta = buscarEntidade(id);
        validarNaoTerminal(alerta);
        alerta.setAlertSituationId(SITUACAO_RESOLVIDO);
        return toResponse(alerta);
    }

    @Transactional
    public PreventiveAlertResponse descartar(UUID id) {
        PreventiveAlert alerta = buscarEntidade(id);
        validarNaoTerminal(alerta);
        alerta.setAlertSituationId(SITUACAO_DESCARTADO);
        return toResponse(alerta);
    }

    private PreventiveAlert buscarEntidade(UUID id) {
        return alertaRepository.findAccessibleById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alerta não encontrado: " + id));
    }

    private void validarNaoTerminal(PreventiveAlert alerta) {
        if (SITUACOES_TERMINAIS.contains(alerta.getAlertSituationId())) {
            throw new IllegalStateException(
                    "Alerta já está em estado terminal (id=" + alerta.getId() + ")");
        }
    }

    private PreventiveAlertResponse buscarAtivoPorPlotETipo(UUID plotId, Integer alertTypeId) {
        return alertaRepository.findAllByPlotIdWithActivePlot(plotId, Pageable.unpaged())
                .stream()
                .filter(a -> a.getAlertTypeId().equals(alertTypeId)
                        && SITUACOES_ABERTOS.contains(a.getAlertSituationId()))
                .findFirst()
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Alerta ativo não encontrado para plot " + plotId));
    }

    private PreventiveAlertResponse toResponse(PreventiveAlert a) {
        return new PreventiveAlertResponse(
                a.getId(), a.getPlotId(), a.getAlertTypeId(), a.getAlertSeverityId(),
                a.getAlertSituationId(), a.getMessage(), a.getObservedNdvi(),
                a.getExpectedNdvi(), a.getDropPct(), a.getIssuedAt(),
                a.getViewedAt(), a.getCreatedAt());
    }
}
