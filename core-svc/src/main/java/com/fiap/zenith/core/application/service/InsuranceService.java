package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.CreateInsuranceRequest;
import com.fiap.zenith.core.application.dto.InsuranceResponse;
import com.fiap.zenith.core.application.exception.DuplicateResourceException;
import com.fiap.zenith.core.application.mapper.InsuranceMapper;
import com.fiap.zenith.core.domain.entity.Insurance;
import com.fiap.zenith.core.domain.repository.InsuranceRepository;
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
public class InsuranceService {

    private final InsuranceRepository insuranceRepository;
    private final InsuranceMapper insuranceMapper;

    public InsuranceService(InsuranceRepository insuranceRepository, InsuranceMapper insuranceMapper) {
        this.insuranceRepository = insuranceRepository;
        this.insuranceMapper = insuranceMapper;
    }

    @Transactional
    @CacheEvict(value = "insurances", allEntries = true)
    public InsuranceResponse criar(CreateInsuranceRequest req) {
        if (insuranceRepository.existsByInsurerIdAndNameAndDeletedAtIsNull(req.insurerId(), req.name())) {
            throw new DuplicateResourceException("Produto '" + req.name() + "' já existe para esta seguradora.");
        }
        Insurance insurance = Insurance.create(req.insurerId(), req.name(), req.description(),
                req.deductiblePct(), req.graceDays(), req.maxCoveragePerHectare(),
                req.baseRatePct(), req.availableStates(), req.insuranceSituationId());
        return insuranceMapper.toResponse(insuranceRepository.save(insurance));
    }

    @Transactional(readOnly = true)
    public InsuranceResponse buscarPorId(UUID id) {
        return insuranceMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    @Cacheable("insurances")
    public Page<InsuranceResponse> listar(Pageable pageable) {
        return insuranceRepository.findAllByDeletedAtIsNull(pageable).map(insuranceMapper::toResponse);
    }

    @Transactional
    @CacheEvict(value = "insurances", allEntries = true)
    public InsuranceResponse atualizar(UUID id, CreateInsuranceRequest req) {
        Insurance insurance = buscarEntidade(id);
        insurance.setName(req.name());
        insurance.setDescription(req.description());
        insurance.setDeductiblePct(req.deductiblePct());
        insurance.setGraceDays(req.graceDays());
        insurance.setMaxCoveragePerHectare(req.maxCoveragePerHectare());
        insurance.setBaseRatePct(req.baseRatePct());
        insurance.setAvailableStates(req.availableStates());
        insurance.setInsuranceSituationId(req.insuranceSituationId());
        insurance.setEditedAt(OffsetDateTime.now());
        return insuranceMapper.toResponse(insurance);
    }

    @Transactional
    @CacheEvict(value = "insurances", allEntries = true)
    public void remover(UUID id) {
        Insurance insurance = buscarEntidade(id);
        insurance.setActive(false);
        insurance.setDeletedAt(OffsetDateTime.now());
    }

    private Insurance buscarEntidade(UUID id) {
        return insuranceRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto de seguro não encontrado: " + id));
    }
}
