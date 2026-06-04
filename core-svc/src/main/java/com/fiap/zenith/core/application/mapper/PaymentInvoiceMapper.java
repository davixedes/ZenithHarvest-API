package com.fiap.zenith.core.application.mapper;

import com.fiap.zenith.core.application.dto.PaymentInvoiceResponse;
import com.fiap.zenith.core.domain.entity.PaymentInvoice;
import org.springframework.stereotype.Component;

@Component
public class PaymentInvoiceMapper {
    public PaymentInvoiceResponse toResponse(PaymentInvoice invoice) {
        return new PaymentInvoiceResponse(invoice.getId(), invoice.getCode(), invoice.getInvoiceNumber(),
                invoice.getUserId(), invoice.getInsurerId(), invoice.getTotalAmount(),
                invoice.getDueDate(), invoice.getIssuedAt(), invoice.getPaidAt(), invoice.getActive());
    }
}
