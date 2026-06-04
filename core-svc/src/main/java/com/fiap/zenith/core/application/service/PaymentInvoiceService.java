package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.CreatePaymentInvoiceRequest;
import com.fiap.zenith.core.application.dto.PaymentInvoiceResponse;
import com.fiap.zenith.core.application.dto.UpdatePaymentInvoiceRequest;
import com.fiap.zenith.core.application.exception.DuplicateResourceException;
import com.fiap.zenith.core.application.mapper.PaymentInvoiceMapper;
import com.fiap.zenith.core.domain.entity.PaymentInvoice;
import com.fiap.zenith.core.domain.repository.InsurerRepository;
import com.fiap.zenith.core.domain.repository.PaymentInvoiceRepository;
import com.fiap.zenith.core.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PaymentInvoiceService {

    private final PaymentInvoiceRepository paymentInvoiceRepository;
    private final UserRepository userRepository;
    private final InsurerRepository insurerRepository;
    private final PaymentInvoiceMapper paymentInvoiceMapper;

    public PaymentInvoiceService(PaymentInvoiceRepository paymentInvoiceRepository,
                                 UserRepository userRepository,
                                 InsurerRepository insurerRepository,
                                 PaymentInvoiceMapper paymentInvoiceMapper) {
        this.paymentInvoiceRepository = paymentInvoiceRepository;
        this.userRepository = userRepository;
        this.insurerRepository = insurerRepository;
        this.paymentInvoiceMapper = paymentInvoiceMapper;
    }

    @Transactional
    public PaymentInvoiceResponse criar(CreatePaymentInvoiceRequest req) {
        userRepository.findByIdAndDeletedAtIsNull(req.userId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + req.userId()));
        validarSeguradora(req.insurerId());
        if (paymentInvoiceRepository.existsByInvoiceNumber(req.invoiceNumber())) {
            throw new DuplicateResourceException("Fatura com número '" + req.invoiceNumber() + "' já existe.");
        }
        PaymentInvoice invoice = PaymentInvoice.create(req.invoiceNumber(), req.userId(), req.insurerId(),
                req.totalAmount(), req.dueDate());
        return paymentInvoiceMapper.toResponse(paymentInvoiceRepository.save(invoice));
    }

    @Transactional(readOnly = true)
    public PaymentInvoiceResponse buscarPorId(UUID id) {
        return paymentInvoiceMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<PaymentInvoiceResponse> listar(UUID userId, Pageable pageable) {
        Page<PaymentInvoice> page = userId != null
                ? paymentInvoiceRepository.findAllByUserIdAndActiveTrue(userId, pageable)
                : paymentInvoiceRepository.findAllByActiveTrue(pageable);
        return page.map(paymentInvoiceMapper::toResponse);
    }

    @Transactional
    public PaymentInvoiceResponse atualizar(UUID id, UpdatePaymentInvoiceRequest req) {
        PaymentInvoice invoice = buscarEntidade(id);
        validarSeguradora(req.insurerId());
        invoice.setInsurerId(req.insurerId());
        invoice.setTotalAmount(req.totalAmount());
        invoice.setDueDate(req.dueDate());
        return paymentInvoiceMapper.toResponse(invoice);
    }

    @Transactional
    public PaymentInvoiceResponse marcarComoPaga(UUID id) {
        PaymentInvoice invoice = buscarEntidade(id);
        if (invoice.getPaidAt() == null) {
            invoice.setPaidAt(OffsetDateTime.now());
        }
        return paymentInvoiceMapper.toResponse(invoice);
    }

    @Transactional
    public void desativar(UUID id) {
        PaymentInvoice invoice = buscarEntidade(id);
        invoice.setActive(false);
    }

    private PaymentInvoice buscarEntidade(UUID id) {
        return paymentInvoiceRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Fatura não encontrada: " + id));
    }

    private void validarSeguradora(UUID insurerId) {
        if (insurerId != null) {
            insurerRepository.findByIdAndDeletedAtIsNull(insurerId)
                    .orElseThrow(() -> new EntityNotFoundException("Seguradora não encontrada: " + insurerId));
        }
    }
}
