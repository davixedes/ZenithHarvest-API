package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.CreatePreventiveAlertRequest;
import com.fiap.zenith.core.application.dto.PreventiveAlertResponse;
import com.fiap.zenith.core.application.dto.UpdatePreventiveAlertSituationRequest;
import com.fiap.zenith.core.application.mapper.PreventiveAlertMapper;
import com.fiap.zenith.core.domain.entity.PreventiveAlert;
import com.fiap.zenith.core.domain.enums.AlertSituation;
import com.fiap.zenith.core.domain.repository.AlertSeverityRepository;
import com.fiap.zenith.core.domain.repository.AlertSituationRepository;
import com.fiap.zenith.core.domain.repository.AlertTypeRepository;
import com.fiap.zenith.core.domain.repository.PlotRepository;
import com.fiap.zenith.core.domain.repository.PreventiveAlertRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PreventiveAlertService {

    private final PreventiveAlertRepository preventiveAlertRepository;
    private final PlotRepository plotRepository;
    private final AlertTypeRepository alertTypeRepository;
    private final AlertSeverityRepository alertSeverityRepository;
    private final AlertSituationRepository alertSituationRepository;
    private final PreventiveAlertMapper preventiveAlertMapper;

    public PreventiveAlertService(PreventiveAlertRepository preventiveAlertRepository,
                                  PlotRepository plotRepository,
                                  AlertTypeRepository alertTypeRepository,
                                  AlertSeverityRepository alertSeverityRepository,
                                  AlertSituationRepository alertSituationRepository,
                                  PreventiveAlertMapper preventiveAlertMapper) {
        this.preventiveAlertRepository = preventiveAlertRepository;
        this.plotRepository = plotRepository;
        this.alertTypeRepository = alertTypeRepository;
        this.alertSeverityRepository = alertSeverityRepository;
        this.alertSituationRepository = alertSituationRepository;
        this.preventiveAlertMapper = preventiveAlertMapper;
    }

    @Transactional
    public PreventiveAlertResponse criar(CreatePreventiveAlertRequest req) {
        plotRepository.findByIdAndDeletedAtIsNull(req.plotId())
                .orElseThrow(() -> new EntityNotFoundException("Talhão não encontrado: " + req.plotId()));
        validarLookups(req.alertTypeId(), req.alertSeverityId(), req.alertSituationId());
        PreventiveAlert alert = PreventiveAlert.create(req.plotId(), req.alertTypeId(), req.alertSeverityId(),
                req.alertSituationId(), req.message(), req.observedNdvi(), req.expectedNdvi(), req.dropPct());
        return preventiveAlertMapper.toResponse(preventiveAlertRepository.save(alert));
    }

    @Transactional(readOnly = true)
    public PreventiveAlertResponse buscarPorId(UUID id) {
        return preventiveAlertMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<PreventiveAlertResponse> listar(UUID plotId, Pageable pageable) {
        Page<PreventiveAlert> page = plotId != null
                ? preventiveAlertRepository.findAllByPlotIdWithActivePlot(plotId, pageable)
                : preventiveAlertRepository.findAllWithActivePlot(pageable);
        return page.map(preventiveAlertMapper::toResponse);
    }

    @Transactional
    public PreventiveAlertResponse marcarComoVisualizado(UUID id) {
        PreventiveAlert alert = buscarEntidade(id);
        if (alert.getViewedAt() == null) {
            alert.setViewedAt(OffsetDateTime.now());
        }
        alert.setAlertSituationId(AlertSituation.VISUALIZADO);
        return preventiveAlertMapper.toResponse(alert);
    }

    @Transactional
    public PreventiveAlertResponse atualizarSituacao(UUID id, UpdatePreventiveAlertSituationRequest req) {
        if (!alertSituationRepository.existsByIdAndActiveTrueAndDeletedAtIsNull(req.alertSituationId())) {
            throw new EntityNotFoundException("Situação de alerta não encontrada: " + req.alertSituationId());
        }
        PreventiveAlert alert = buscarEntidade(id);
        alert.setAlertSituationId(req.alertSituationId());
        return preventiveAlertMapper.toResponse(alert);
    }

    private PreventiveAlert buscarEntidade(UUID id) {
        return preventiveAlertRepository.findAccessibleById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alerta preventivo não encontrado: " + id));
    }

    private void validarLookups(Integer alertTypeId, Integer alertSeverityId, Integer alertSituationId) {
        if (!alertTypeRepository.existsByIdAndActiveTrue(alertTypeId)) {
            throw new EntityNotFoundException("Tipo de alerta não encontrado: " + alertTypeId);
        }
        if (!alertSeverityRepository.existsById(alertSeverityId)) {
            throw new EntityNotFoundException("Severidade de alerta não encontrada: " + alertSeverityId);
        }
        if (!alertSituationRepository.existsByIdAndActiveTrueAndDeletedAtIsNull(alertSituationId)) {
            throw new EntityNotFoundException("Situação de alerta não encontrada: " + alertSituationId);
        }
    }
}
