package com.fiap.zenith.core.application.mapper;

import com.fiap.zenith.core.application.dto.PolicyResponse;
import com.fiap.zenith.core.domain.entity.Policy;
import org.springframework.stereotype.Component;

@Component
public class PolicyMapper {
    public PolicyResponse toResponse(Policy p) {
        return new PolicyResponse(p.getId(), p.getCode(), p.getPolicyNumber(),
                p.getInsuranceQuoteId(), p.getPlotId(), p.getInsurerId(), p.getInsuranceId(),
                p.getPolicySituationId(), p.getInsuredAmount(), p.getTotalPremium(),
                p.getMonthlyPremium(), p.getDeductiblePct(), p.getMaxCoverage(),
                p.getAccumulatedPaid(), p.getStartDate(), p.getEndDate(),
                p.getContractedAt(), p.getCancelledAt(), p.getCreatedAt(), p.getEditedAt());
    }
}
