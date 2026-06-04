package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.CreatePlotRequest;
import com.fiap.zenith.core.application.dto.PlotResponse;
import com.fiap.zenith.core.application.dto.UpdatePlotRequest;
import com.fiap.zenith.core.application.exception.DuplicateResourceException;
import com.fiap.zenith.core.application.mapper.PlotMapper;
import com.fiap.zenith.core.domain.entity.Plot;
import com.fiap.zenith.core.domain.repository.PlotRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PlotService {

    private final PlotRepository plotRepository;
    private final PlotMapper plotMapper;

    public PlotService(PlotRepository plotRepository, PlotMapper plotMapper) {
        this.plotRepository = plotRepository;
        this.plotMapper = plotMapper;
    }

    @Transactional
    public PlotResponse criar(CreatePlotRequest req) {
        if (req.identifier() != null
                && plotRepository.existsByFarmIdAndIdentifierAndDeletedAtIsNull(req.farmId(), req.identifier())) {
            throw new DuplicateResourceException(
                    "Talhão com identificador '" + req.identifier() + "' já existe nesta fazenda.");
        }
        Plot plot = Plot.create(req.farmId(), req.cropId(), req.plotSituationId(),
                req.productionSystemId(), req.identifier(), req.areaHectares(),
                req.plantingDate(), req.estimatedHarvestDate(), req.cycleDays(),
                req.seedVariety(), req.polygonWkt());
        return plotMapper.toResponse(plotRepository.save(plot));
    }

    @Transactional(readOnly = true)
    public PlotResponse buscarPorId(UUID id) {
        return plotMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<PlotResponse> listarPorFazenda(UUID farmId, Pageable pageable) {
        return plotRepository.findAllByFarmIdAndDeletedAtIsNull(farmId, pageable)
                .map(plotMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PlotResponse> listar(Pageable pageable) {
        return plotRepository.findAllByDeletedAtIsNull(pageable).map(plotMapper::toResponse);
    }

    @Transactional
    public PlotResponse atualizar(UUID id, UpdatePlotRequest req) {
        Plot plot = buscarEntidade(id);
        plot.setCropId(req.cropId());
        plot.setPlotSituationId(req.plotSituationId());
        plot.setProductionSystemId(req.productionSystemId());
        plot.setIdentifier(req.identifier());
        plot.setAreaHectares(req.areaHectares());
        plot.setPlantingDate(req.plantingDate());
        plot.setEstimatedHarvestDate(req.estimatedHarvestDate());
        plot.setCycleDays(req.cycleDays());
        plot.setSeedVariety(req.seedVariety());
        plot.setPolygonWkt(req.polygonWkt());
        plot.setEditedAt(OffsetDateTime.now());
        return plotMapper.toResponse(plot);
    }

    @Transactional
    public void remover(UUID id) {
        Plot plot = buscarEntidade(id);
        plot.setDeletedAt(OffsetDateTime.now());
    }

    private Plot buscarEntidade(UUID id) {
        return plotRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Talhão não encontrado: " + id));
    }
}
