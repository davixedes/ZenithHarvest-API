package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.CreatePolicyItemRequest;
import com.fiap.zenith.core.application.dto.PolicyItemResponse;
import com.fiap.zenith.core.application.dto.UpdatePolicyItemRequest;
import com.fiap.zenith.core.application.exception.DuplicateResourceException;
import com.fiap.zenith.core.application.mapper.PolicyItemMapper;
import com.fiap.zenith.core.domain.entity.PolicyItem;
import com.fiap.zenith.core.domain.repository.ClaimEventTypeRepository;
import com.fiap.zenith.core.domain.repository.PolicyItemRepository;
import com.fiap.zenith.core.domain.repository.PolicyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PolicyItemService {

    private final PolicyItemRepository policyItemRepository;
    private final PolicyRepository policyRepository;
    private final ClaimEventTypeRepository claimEventTypeRepository;
    private final PolicyItemMapper policyItemMapper;

    public PolicyItemService(PolicyItemRepository policyItemRepository, PolicyRepository policyRepository,
                             ClaimEventTypeRepository claimEventTypeRepository,
                             PolicyItemMapper policyItemMapper) {
        this.policyItemRepository = policyItemRepository;
        this.policyRepository = policyRepository;
        this.claimEventTypeRepository = claimEventTypeRepository;
        this.policyItemMapper = policyItemMapper;
    }

    @Transactional
    public PolicyItemResponse criar(CreatePolicyItemRequest req) {
        policyRepository.findByIdAndDeletedAtIsNull(req.policyId())
                .orElseThrow(() -> new EntityNotFoundException("Apólice não encontrada: " + req.policyId()));
        if (!claimEventTypeRepository.existsByIdAndActiveTrueAndDeletedAtIsNull(req.claimEventTypeId())) {
            throw new EntityNotFoundException("Tipo de evento de sinistro não encontrado: " + req.claimEventTypeId());
        }
        if (policyItemRepository.existsByPolicyIdAndClaimEventTypeId(req.policyId(), req.claimEventTypeId())) {
            throw new DuplicateResourceException("Cobertura para o evento '" + req.claimEventTypeId()
                    + "' já existe na apólice '" + req.policyId() + "'.");
        }
        PolicyItem item = PolicyItem.create(req.policyId(), req.claimEventTypeId(), req.coveragePct(),
                req.maxCoverageAmount(), req.notes());
        return policyItemMapper.toResponse(policyItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public PolicyItemResponse buscarPorId(UUID id) {
        return policyItemMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<PolicyItemResponse> listar(UUID policyId, Pageable pageable) {
        Page<PolicyItem> page = policyId != null
                ? policyItemRepository.findAllByPolicyIdWithActivePolicy(policyId, pageable)
                : policyItemRepository.findAllWithActivePolicy(pageable);
        return page.map(policyItemMapper::toResponse);
    }

    @Transactional
    public PolicyItemResponse atualizar(UUID id, UpdatePolicyItemRequest req) {
        PolicyItem item = buscarEntidade(id);
        if (req.coveragePct() != null) {
            item.setCoveragePct(req.coveragePct());
        }
        item.setMaxCoverageAmount(req.maxCoverageAmount());
        item.setNotes(req.notes());
        return policyItemMapper.toResponse(item);
    }

    @Transactional
    public void remover(UUID id) {
        policyItemRepository.delete(buscarEntidade(id));
    }

    private PolicyItem buscarEntidade(UUID id) {
        return policyItemRepository.findAccessibleById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item de apólice não encontrado: " + id));
    }
}
