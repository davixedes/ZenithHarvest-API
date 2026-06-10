package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.CreatePolicyRequest;
import com.fiap.zenith.core.application.dto.PolicyResponse;
import com.fiap.zenith.core.application.mapper.PolicyMapper;
import com.fiap.zenith.core.domain.entity.Policy;
import com.fiap.zenith.core.domain.enums.PolicySituation;
import com.fiap.zenith.core.domain.repository.PolicyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.Year;
import java.util.UUID;

@Service
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyMapper policyMapper;

    public PolicyService(PolicyRepository policyRepository, PolicyMapper policyMapper) {
        this.policyRepository = policyRepository;
        this.policyMapper = policyMapper;
    }

    @Transactional
    public PolicyResponse criar(CreatePolicyRequest req) {
        // PolicyNumber é o protocolo da apólice — gerado pelo servidor (não vem do cliente).
        String policyNumber = "ZH-APO-" + Year.now() + "-" + System.currentTimeMillis();
        Policy policy = Policy.create(policyNumber, req.insuranceQuoteId(), req.plotId(),
                req.insurerId(), req.insuranceId(), req.policySituationId(),
                req.insuredAmount(), req.totalPremium(), req.monthlyPremium(),
                req.deductiblePct(), req.maxCoverage(), req.startDate(), req.endDate());
        return policyMapper.toResponse(policyRepository.save(policy));
    }

    @Transactional(readOnly = true)
    public PolicyResponse buscarPorId(UUID id) {
        return policyMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<PolicyResponse> listar(Pageable pageable) {
        return policyRepository.findAllByDeletedAtIsNull(pageable).map(policyMapper::toResponse);
    }

    @Transactional
    public void cancelar(UUID id) {
        Policy policy = buscarEntidade(id);
        policy.setPolicySituationId(PolicySituation.CANCELADA);
        policy.setCancelledAt(OffsetDateTime.now());
        policy.setEditedAt(OffsetDateTime.now());
    }

    @Transactional
    public void remover(UUID id) {
        Policy policy = buscarEntidade(id);
        policy.setDeletedAt(OffsetDateTime.now());
    }

    private Policy buscarEntidade(UUID id) {
        return policyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Apólice não encontrada: " + id));
    }
}
