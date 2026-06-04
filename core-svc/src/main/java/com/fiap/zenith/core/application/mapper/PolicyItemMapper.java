package com.fiap.zenith.core.application.mapper;

import com.fiap.zenith.core.application.dto.PolicyItemResponse;
import com.fiap.zenith.core.domain.entity.PolicyItem;
import org.springframework.stereotype.Component;

@Component
public class PolicyItemMapper {
    public PolicyItemResponse toResponse(PolicyItem item) {
        return new PolicyItemResponse(item.getId(), item.getPolicyId(), item.getClaimEventTypeId(),
                item.getCoveragePct(), item.getMaxCoverageAmount(), item.getNotes());
    }
}
