package com.fiap.zenith.core.application.mapper;

import com.fiap.zenith.core.application.dto.InsuranceQuoteResponse;
import com.fiap.zenith.core.domain.entity.InsuranceQuote;
import org.springframework.stereotype.Component;

@Component
public class InsuranceQuoteMapper {
    public InsuranceQuoteResponse toResponse(InsuranceQuote q) {
        return new InsuranceQuoteResponse(q.getId(), q.getCode(), q.getUserId(), q.getPlotId(),
                q.getInsuranceId(), q.getQuoteSituationId(), q.getInsuredAmount(),
                q.getTotalPremium(), q.getMonthlyPremium(), q.getRegionalFactor(),
                q.getHistoryFactor(), q.getValidUntil(), q.getAcceptedAt(),
                q.getCreatedAt(), q.getEditedAt());
    }
}
