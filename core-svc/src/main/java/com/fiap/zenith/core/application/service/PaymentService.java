package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.CreatePaymentRequest;
import com.fiap.zenith.core.application.dto.PaymentResponse;
import com.fiap.zenith.core.application.mapper.PaymentMapper;
import com.fiap.zenith.core.domain.entity.Payment;
import com.fiap.zenith.core.domain.enums.ClaimSituation;
import com.fiap.zenith.core.domain.enums.PaymentSituation;
import com.fiap.zenith.core.domain.repository.ClaimRepository;
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
    private final ClaimRepository claimRepository;
    private final PaymentMapper paymentMapper;

    public PaymentService(PaymentRepository paymentRepository,
                          ClaimRepository claimRepository,
                          PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.claimRepository = claimRepository;
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

    /**
     * Confirma pagamento via PIX: transita para PaymentSituation.CONFIRMADO e registra PSP transaction id.
     * Se o pagamento for de um sinistro (claimId presente), fecha o ciclo movendo o sinistro para
     * ClaimSituation.PAGO e carimbando paidAt — a indenização caiu na conta do produtor.
     */
    @Transactional
    public PaymentResponse confirmar(UUID id) {
        Payment payment = buscarEntidade(id);
        OffsetDateTime agora = OffsetDateTime.now();
        // O id da transação é emitido pelo PSP (provedor PIX) ao liquidar — aqui simulamos
        // no padrão EndToEndId do PIX ("E" + timestamp + sufixo), não vem do cliente.
        String pspTransactionId = "E" + System.currentTimeMillis()
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        payment.setPaymentSituationId(PaymentSituation.CONFIRMADO);
        payment.setPspTransactionId(pspTransactionId);
        payment.setConfirmedAt(agora);
        payment.setEditedAt(agora);

        if (payment.getClaimId() != null) {
            claimRepository.findByIdAndDeletedAtIsNull(payment.getClaimId()).ifPresent(claim -> {
                claim.setClaimSituationId(ClaimSituation.PAGO);
                claim.setPaidAt(agora);
                claim.setEditedAt(agora);
            });
        }

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
