package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.CreatePaymentRequest;
import com.fiap.zenith.core.application.dto.PaymentResponse;
import com.fiap.zenith.core.application.mapper.PaymentMapper;
import com.fiap.zenith.core.domain.entity.Payment;
import com.fiap.zenith.core.domain.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    @Transactional
    public PaymentResponse criar(CreatePaymentRequest req) {
        Payment payment = Payment.create(req.paymentTypeId(), req.paymentSituationId(),
                req.claimId(), req.policyId(), req.paymentInvoiceId(),
                req.amount(), req.pixKey());
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public PaymentResponse buscarPorId(UUID id) {
        return paymentMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> listar(Pageable pageable) {
        return paymentRepository.findAllByDeletedAtIsNull(pageable).map(paymentMapper::toResponse);
    }

    /** Confirma pagamento via PIX: situação → 3 (Confirmado), registra PSP transaction id. */
    @Transactional
    public PaymentResponse confirmar(UUID id, String pspTransactionId) {
        Payment payment = buscarEntidade(id);
        payment.setPaymentSituationId(3); // Confirmado
        payment.setPspTransactionId(pspTransactionId);
        payment.setConfirmedAt(OffsetDateTime.now());
        payment.setEditedAt(OffsetDateTime.now());
        return paymentMapper.toResponse(payment);
    }

    @Transactional
    public void remover(UUID id) {
        Payment payment = buscarEntidade(id);
        payment.setDeletedAt(OffsetDateTime.now());
    }

    private Payment buscarEntidade(UUID id) {
        return paymentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado: " + id));
    }
}
