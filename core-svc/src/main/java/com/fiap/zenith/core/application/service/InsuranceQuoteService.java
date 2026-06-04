package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.CreateInsuranceQuoteRequest;
import com.fiap.zenith.core.application.dto.InsuranceQuoteResponse;
import com.fiap.zenith.core.application.mapper.InsuranceQuoteMapper;
import com.fiap.zenith.core.domain.entity.InsuranceQuote;
import com.fiap.zenith.core.domain.enums.SituacaoIds;
import com.fiap.zenith.core.domain.repository.InsuranceQuoteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class InsuranceQuoteService {

    private final InsuranceQuoteRepository quoteRepository;
    private final InsuranceQuoteMapper quoteMapper;

    public InsuranceQuoteService(InsuranceQuoteRepository quoteRepository,
                                  InsuranceQuoteMapper quoteMapper) {
        this.quoteRepository = quoteRepository;
        this.quoteMapper = quoteMapper;
    }

    @Transactional
    public InsuranceQuoteResponse criar(CreateInsuranceQuoteRequest req) {
        InsuranceQuote quote = InsuranceQuote.create(req.userId(), req.plotId(), req.insuranceId(),
                req.quoteSituationId(), req.insuredAmount(), req.totalPremium(),
                req.monthlyPremium(), req.regionalFactor(), req.historyFactor(), req.validUntil());
        return quoteMapper.toResponse(quoteRepository.save(quote));
    }

    @Transactional(readOnly = true)
    public InsuranceQuoteResponse buscarPorId(UUID id) {
        return quoteMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<InsuranceQuoteResponse> listar(Pageable pageable) {
        return quoteRepository.findAllByDeletedAtIsNull(pageable).map(quoteMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<InsuranceQuoteResponse> listarPorUsuario(UUID userId, Pageable pageable) {
        return quoteRepository.findAllByUserIdAndDeletedAtIsNull(userId, pageable)
                .map(quoteMapper::toResponse);
    }

    /** Aceita a cotação: muda situação para 2 (Aceita) e registra timestamp. */
    @Transactional
    public InsuranceQuoteResponse aceitar(UUID id) {
        InsuranceQuote quote = buscarEntidade(id);
        quote.setQuoteSituationId(SituacaoIds.COTACAO_ACEITA);
        quote.setAcceptedAt(OffsetDateTime.now());
        quote.setEditedAt(OffsetDateTime.now());
        return quoteMapper.toResponse(quote);
    }

    @Transactional
    public void remover(UUID id) {
        InsuranceQuote quote = buscarEntidade(id);
        quote.setDeletedAt(OffsetDateTime.now());
    }

    private InsuranceQuote buscarEntidade(UUID id) {
        return quoteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Cotação não encontrada: " + id));
    }
}
