package com.fiap.zenith.core.application.mapper;

import com.fiap.zenith.core.application.dto.ClaimResponse;
import com.fiap.zenith.core.domain.entity.Claim;
import org.springframework.stereotype.Component;

@Component
public class ClaimMapper {
    public ClaimResponse toResponse(Claim c) {
        return new ClaimResponse(c.getId(), c.getCode(), c.getClaimNumber(), c.getPolicyId(),
                c.getClaimSituationId(), c.getCategoryId(), c.getSubCategoryId(),
                c.getDescription(), c.getPhotoUrl(), c.getOpeningGpsLat(), c.getOpeningGpsLng(),
                c.getNdviBefore(), c.getNdviAfter(), c.getTotalLossPct(),
                c.getTotalAffectedAreaHa(), c.getCalculatedAmount(), c.getApprovedAmount(),
                c.getMlConfidenceScore(), c.getFraudFlag(), c.getRejectionReasonId(),
                c.getApprovedAt(), c.getPaidAt(), c.getCreatedAt(), c.getEditedAt());
    }
}
