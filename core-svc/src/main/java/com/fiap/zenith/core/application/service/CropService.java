package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.CreateCropRequest;
import com.fiap.zenith.core.application.dto.CropResponse;
import com.fiap.zenith.core.application.dto.UpdateCropRequest;
import com.fiap.zenith.core.application.exception.DuplicateResourceException;
import com.fiap.zenith.core.application.mapper.CropMapper;
import com.fiap.zenith.core.domain.entity.Crop;
import com.fiap.zenith.core.domain.repository.CropRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class CropService {

    private final CropRepository cropRepository;
    private final CropMapper cropMapper;

    public CropService(CropRepository cropRepository, CropMapper cropMapper) {
        this.cropRepository = cropRepository;
        this.cropMapper = cropMapper;
    }

    @Transactional
    @CacheEvict(value = "crops", allEntries = true)
    public CropResponse criar(CreateCropRequest req) {
        if (cropRepository.existsByNameAndDeletedAtIsNull(req.name())) {
            throw new DuplicateResourceException("Cultura com nome '" + req.name() + "' já cadastrada.");
        }
        Crop crop = Crop.create(req.name(), req.scientificName(), req.averageCycleDays(),
                req.expectedNdviMin(), req.expectedNdviMax(), req.averageValuePerHectare(),
                req.droughtVulnerability(), req.frostVulnerability());
        return cropMapper.toResponse(cropRepository.save(crop));
    }

    @Transactional(readOnly = true)
    public CropResponse buscarPorId(UUID id) {
        return cropMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    @Cacheable("crops")
    public Page<CropResponse> listar(Pageable pageable) {
        return cropRepository.findAllByDeletedAtIsNull(pageable).map(cropMapper::toResponse);
    }

    @Transactional
    @CacheEvict(value = "crops", allEntries = true)
    public CropResponse atualizar(UUID id, UpdateCropRequest req) {
        Crop crop = buscarEntidade(id);
        crop.setName(req.name());
        crop.setScientificName(req.scientificName());
        crop.setAverageCycleDays(req.averageCycleDays());
        crop.setExpectedNdviMin(req.expectedNdviMin());
        crop.setExpectedNdviMax(req.expectedNdviMax());
        crop.setAverageValuePerHectare(req.averageValuePerHectare());
        crop.setDroughtVulnerability(req.droughtVulnerability());
        crop.setFrostVulnerability(req.frostVulnerability());
        crop.setEditedAt(OffsetDateTime.now());
        return cropMapper.toResponse(crop);
    }

    @Transactional
    @CacheEvict(value = "crops", allEntries = true)
    public void remover(UUID id) {
        Crop crop = buscarEntidade(id);
        crop.setStatus(false);
        crop.setDeletedAt(OffsetDateTime.now());
    }

    private Crop buscarEntidade(UUID id) {
        return cropRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Cultura não encontrada: " + id));
    }
}
