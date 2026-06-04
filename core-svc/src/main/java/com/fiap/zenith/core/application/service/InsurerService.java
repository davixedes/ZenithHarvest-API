package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.CreateInsurerRequest;
import com.fiap.zenith.core.application.dto.InsurerResponse;
import com.fiap.zenith.core.application.exception.DuplicateResourceException;
import com.fiap.zenith.core.application.mapper.InsurerMapper;
import com.fiap.zenith.core.domain.entity.Insurer;
import com.fiap.zenith.core.domain.repository.InsurerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class InsurerService {

    private final InsurerRepository insurerRepository;
    private final InsurerMapper insurerMapper;

    public InsurerService(InsurerRepository insurerRepository, InsurerMapper insurerMapper) {
        this.insurerRepository = insurerRepository;
        this.insurerMapper = insurerMapper;
    }

    @Transactional
    public InsurerResponse criar(CreateInsurerRequest req) {
        if (insurerRepository.existsByCnpjAndDeletedAtIsNull(req.cnpj())) {
            throw new DuplicateResourceException("Seguradora com CNPJ '" + req.cnpj() + "' já cadastrada.");
        }
        Insurer insurer = Insurer.create(req.corporateName(), req.tradeName(), req.cnpj(),
                req.susepCode(), req.commercialEmail(), req.phone(), req.logoUrl(),
                req.adminFeePct(), req.takeRatePct(), req.insurerSituationId(), req.accreditedAt());
        return insurerMapper.toResponse(insurerRepository.save(insurer));
    }

    @Transactional(readOnly = true)
    public InsurerResponse buscarPorId(UUID id) {
        return insurerMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<InsurerResponse> listar(Pageable pageable) {
        return insurerRepository.findAllByDeletedAtIsNull(pageable).map(insurerMapper::toResponse);
    }

    @Transactional
    public InsurerResponse atualizar(UUID id, CreateInsurerRequest req) {
        Insurer insurer = buscarEntidade(id);
        insurer.setCorporateName(req.corporateName());
        insurer.setTradeName(req.tradeName());
        insurer.setCommercialEmail(req.commercialEmail());
        insurer.setPhone(req.phone());
        insurer.setLogoUrl(req.logoUrl());
        insurer.setAdminFeePct(req.adminFeePct());
        insurer.setTakeRatePct(req.takeRatePct());
        insurer.setInsurerSituationId(req.insurerSituationId());
        insurer.setAccreditedAt(req.accreditedAt());
        insurer.setEditedAt(OffsetDateTime.now());
        return insurerMapper.toResponse(insurer);
    }

    @Transactional
    public void remover(UUID id) {
        Insurer insurer = buscarEntidade(id);
        insurer.setActive(false);
        insurer.setDeletedAt(OffsetDateTime.now());
    }

    private Insurer buscarEntidade(UUID id) {
        return insurerRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Seguradora não encontrada: " + id));
    }
}
