package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.ClaimItemResponse;
import com.fiap.zenith.core.application.dto.CreateClaimItemRequest;
import com.fiap.zenith.core.application.dto.UpdateClaimItemRequest;
import com.fiap.zenith.core.application.mapper.ClaimItemMapper;
import com.fiap.zenith.core.domain.entity.ClaimItem;
import com.fiap.zenith.core.domain.repository.ClaimEventTypeRepository;
import com.fiap.zenith.core.domain.repository.ClaimRepository;
import com.fiap.zenith.core.domain.repository.ClaimItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ClaimItemService {

    private final ClaimItemRepository claimItemRepository;
    private final ClaimRepository claimRepository;
    private final ClaimEventTypeRepository claimEventTypeRepository;
    private final ClaimItemMapper claimItemMapper;

    public ClaimItemService(ClaimItemRepository claimItemRepository, ClaimRepository claimRepository,
                            ClaimEventTypeRepository claimEventTypeRepository,
                            ClaimItemMapper claimItemMapper) {
        this.claimItemRepository = claimItemRepository;
        this.claimRepository = claimRepository;
        this.claimEventTypeRepository = claimEventTypeRepository;
        this.claimItemMapper = claimItemMapper;
    }

    @Transactional
    public ClaimItemResponse criar(CreateClaimItemRequest req) {
        claimRepository.findByIdAndDeletedAtIsNull(req.claimId())
                .orElseThrow(() -> new EntityNotFoundException("Sinistro não encontrado: " + req.claimId()));
        if (!claimEventTypeRepository.existsByIdAndActiveTrueAndDeletedAtIsNull(req.claimEventTypeId())) {
            throw new EntityNotFoundException("Tipo de evento de sinistro não encontrado: " + req.claimEventTypeId());
        }
        ClaimItem item = ClaimItem.create(req.claimId(), req.claimEventTypeId(), req.affectedAreaHa(),
                req.lossPct(), req.ndviBefore(), req.ndviAfter(), req.itemAmount(), req.description());
        return claimItemMapper.toResponse(claimItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public ClaimItemResponse buscarPorId(UUID id) {
        return claimItemMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<ClaimItemResponse> listar(UUID claimId, Pageable pageable) {
        Page<ClaimItem> page = claimId != null
                ? claimItemRepository.findAllByClaimIdWithActiveClaim(claimId, pageable)
                : claimItemRepository.findAllWithActiveClaim(pageable);
        return page.map(claimItemMapper::toResponse);
    }

    @Transactional
    public ClaimItemResponse atualizar(UUID id, UpdateClaimItemRequest req) {
        ClaimItem item = buscarEntidade(id);
        item.setAffectedAreaHa(req.affectedAreaHa());
        item.setLossPct(req.lossPct());
        item.setNdviBefore(req.ndviBefore());
        item.setNdviAfter(req.ndviAfter());
        item.setItemAmount(req.itemAmount());
        item.setDescription(req.description());
        return claimItemMapper.toResponse(item);
    }

    @Transactional
    public void remover(UUID id) {
        claimItemRepository.delete(buscarEntidade(id));
    }

    private ClaimItem buscarEntidade(UUID id) {
        return claimItemRepository.findAccessibleById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item de sinistro não encontrado: " + id));
    }
}
