package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.CreateClaimRequest;
import com.fiap.zenith.core.application.dto.ClaimResponse;
import com.fiap.zenith.core.application.mapper.ClaimMapper;
import com.fiap.zenith.core.domain.entity.Claim;
import com.fiap.zenith.core.domain.entity.Plot;
import com.fiap.zenith.core.domain.entity.Policy;
import com.fiap.zenith.core.domain.enums.ClaimSituation;
import com.fiap.zenith.core.domain.repository.ClaimRepository;
import com.fiap.zenith.core.domain.repository.PlotRepository;
import com.fiap.zenith.core.domain.repository.PolicyRepository;
import com.fiap.zenith.core.infra.messaging.ClaimEventPublisher;
import com.fiap.zenith.core.infra.messaging.SinistroAbertoEvent;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ClaimService {

    /** Conversão hectare → m² para o evento de análise. */
    private static final BigDecimal M2_POR_HECTARE = new BigDecimal("10000");

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final PlotRepository plotRepository;
    private final ClaimMapper claimMapper;
    private final ClaimEventPublisher eventPublisher;

    public ClaimService(ClaimRepository claimRepository,
                         PolicyRepository policyRepository,
                         PlotRepository plotRepository,
                         ClaimMapper claimMapper,
                         ClaimEventPublisher eventPublisher) {
        this.claimRepository = claimRepository;
        this.policyRepository = policyRepository;
        this.plotRepository = plotRepository;
        this.claimMapper = claimMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Cria o sinistro e publica o evento {@code sinistro.aberto} no RabbitMQ
     * para que o analise-svc processe a análise satelital + IA.
     * O evento carrega valor segurado e área do talhão — o analise-svc não
     * acessa Policy/Plot, calcula só com o que recebe.
     */
    @Transactional
    public ClaimResponse abrir(CreateClaimRequest req) {
        Policy policy = policyRepository.findByIdAndDeletedAtIsNull(req.policyId())
                .orElseThrow(() -> new EntityNotFoundException("Apólice não encontrada: " + req.policyId()));
        Plot plot = plotRepository.findByIdAndDeletedAtIsNull(policy.getPlotId())
                .orElseThrow(() -> new EntityNotFoundException("Talhão não encontrado: " + policy.getPlotId()));

        Claim claim = Claim.create(req.claimNumber(), req.policyId(), req.claimSituationId(),
                req.categoryId(), req.subCategoryId(), req.description(), req.photoUrl(),
                req.openingGpsLat(), req.openingGpsLng(), req.ndviBefore());
        Claim saved = claimRepository.save(claim);

        BigDecimal plotAreaM2 = plot.getAreaHectares() != null
                ? plot.getAreaHectares().multiply(M2_POR_HECTARE)
                : null;

        eventPublisher.publicarSinistroAberto(new SinistroAbertoEvent(
                saved.getId(), saved.getClaimNumber(), saved.getPolicyId(),
                policy.getPlotId(), saved.getCategoryId(), saved.getSubCategoryId(),
                saved.getNdviBefore(), saved.getOpeningGpsLat(), saved.getOpeningGpsLng(),
                saved.getDescription(), policy.getInsuredAmount(), plotAreaM2));

        return claimMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ClaimResponse buscarPorId(UUID id) {
        return claimMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<ClaimResponse> listar(Pageable pageable) {
        return claimRepository.findAllByDeletedAtIsNull(pageable).map(claimMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ClaimResponse> listarPorApolice(UUID policyId, Pageable pageable) {
        return claimRepository.findAllByPolicyIdAndDeletedAtIsNull(policyId, pageable)
                .map(claimMapper::toResponse);
    }

    /** Aprova manualmente o sinistro (ClaimSituation.APROVADO no seed canônico). */
    @Transactional
    public ClaimResponse aprovar(UUID id, java.math.BigDecimal approvedAmount) {
        Claim claim = buscarEntidade(id);
        claim.setClaimSituationId(ClaimSituation.APROVADO);
        claim.setApprovedAmount(approvedAmount);
        claim.setApprovedAt(OffsetDateTime.now());
        claim.setEditedAt(OffsetDateTime.now());
        return claimMapper.toResponse(claim);
    }

    /** Rejeita o sinistro com motivo (ClaimSituation.REJEITADO no seed canônico). */
    @Transactional
    public ClaimResponse rejeitar(UUID id, Integer rejectionReasonId) {
        Claim claim = buscarEntidade(id);
        claim.setClaimSituationId(ClaimSituation.REJEITADO);
        claim.setRejectionReasonId(rejectionReasonId);
        claim.setEditedAt(OffsetDateTime.now());
        return claimMapper.toResponse(claim);
    }

    @Transactional
    public void remover(UUID id) {
        Claim claim = buscarEntidade(id);
        claim.setDeletedAt(OffsetDateTime.now());
    }

    private Claim buscarEntidade(UUID id) {
        return claimRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Sinistro não encontrado: " + id));
    }
}
