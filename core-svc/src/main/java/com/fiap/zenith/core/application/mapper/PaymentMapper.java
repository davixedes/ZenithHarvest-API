package com.fiap.zenith.core.application.mapper;

import com.fiap.zenith.core.application.dto.PaymentResponse;
import com.fiap.zenith.core.domain.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(p.getId(), p.getCode(), p.getPaymentTypeId(),
                p.getPaymentSituationId(), p.getClaimId(), p.getPolicyId(),
                p.getPaymentInvoiceId(), p.getAmount(), p.getPixKey(),
                p.getSentAt(), p.getConfirmedAt(), p.getPspTransactionId(),
                p.getAttempts(), p.getFailureReason(), p.getCreatedAt(), p.getEditedAt());
    }
}
