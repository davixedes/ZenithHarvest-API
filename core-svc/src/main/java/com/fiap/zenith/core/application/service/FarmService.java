package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.CreateFarmRequest;
import com.fiap.zenith.core.application.dto.FarmResponse;
import com.fiap.zenith.core.application.dto.UpdateFarmRequest;
import com.fiap.zenith.core.application.exception.DuplicateResourceException;
import com.fiap.zenith.core.domain.entity.Farm;
import com.fiap.zenith.core.domain.repository.FarmRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Regras de negócio do recurso Farm. Orquestra persistência e mapeamento entity ↔ DTO.
 * Controllers nunca tocam o repositório diretamente.
 */
@Service
public class FarmService {

    private final FarmRepository farmRepository;

    public FarmService(FarmRepository farmRepository) {
        this.farmRepository = farmRepository;
    }

    @Transactional
    public FarmResponse criar(CreateFarmRequest req) {
        if (farmRepository.existsByCarRegistrationAndDeletedAtIsNull(req.carRegistration())) {
            throw new DuplicateResourceException(
                    "Já existe uma fazenda com o CAR " + req.carRegistration());
        }

        Farm farm = Farm.create(
                req.userId(),
                req.name(),
                req.carRegistration(),
                req.nirf(),
                req.latitude(),
                req.longitude(),
                req.totalAreaHectares(),
                req.state(),
                req.biomeId(),
                req.propertyType(),
                req.polygonWkt());

        Farm saved = farmRepository.save(farm);
        return FarmResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public FarmResponse buscarPorId(UUID id) {
        return FarmResponse.from(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<FarmResponse> listar(Pageable pageable) {
        return farmRepository.findAllByDeletedAtIsNull(pageable).map(FarmResponse::from);
    }

    @Transactional
    public FarmResponse atualizar(UUID id, UpdateFarmRequest req) {
        Farm farm = buscarEntidade(id);
        farm.setName(req.name());
        farm.setCarRegistration(req.carRegistration());
        farm.setNirf(req.nirf());
        farm.setLatitude(req.latitude());
        farm.setLongitude(req.longitude());
        farm.setTotalAreaHectares(req.totalAreaHectares());
        farm.setState(req.state());
        farm.setBiomeId(req.biomeId());
        farm.setPropertyType(req.propertyType());
        farm.setPolygonWkt(req.polygonWkt());
        farm.setEditedAt(OffsetDateTime.now());

        return FarmResponse.from(farm);
    }

    /** Soft delete: marca DeletedAt e inativa, mantendo a linha para auditoria. */
    @Transactional
    public void remover(UUID id) {
        Farm farm = buscarEntidade(id);
        farm.setActive(false);
        farm.setDeletedAt(OffsetDateTime.now());
    }

    private Farm buscarEntidade(UUID id) {
        return farmRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Fazenda não encontrada: " + id));
    }
}
