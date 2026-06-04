package com.fiap.zenith.core.application.mapper;

import com.fiap.zenith.core.application.dto.InsuranceResponse;
import com.fiap.zenith.core.domain.entity.Insurance;
import org.springframework.stereotype.Component;

@Component
public class InsuranceMapper {
    public InsuranceResponse toResponse(Insurance ins) {
        return new InsuranceResponse(ins.getId(), ins.getCode(), ins.getInsurerId(), ins.getName(),
                ins.getDescription(), ins.getDeductiblePct(), ins.getGraceDays(),
                ins.getMaxCoveragePerHectare(), ins.getBaseRatePct(), ins.getAvailableStates(),
                ins.getInsuranceSituationId(), ins.getActive(), ins.getCreatedAt(), ins.getEditedAt());
    }
}
