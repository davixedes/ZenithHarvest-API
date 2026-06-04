package com.fiap.zenith.core.application.mapper;

import com.fiap.zenith.core.application.dto.ClaimItemResponse;
import com.fiap.zenith.core.domain.entity.ClaimItem;
import org.springframework.stereotype.Component;

@Component
public class ClaimItemMapper {
    public ClaimItemResponse toResponse(ClaimItem item) {
        return new ClaimItemResponse(item.getId(), item.getClaimId(), item.getClaimEventTypeId(),
                item.getAffectedAreaHa(), item.getLossPct(), item.getNdviBefore(),
                item.getNdviAfter(), item.getItemAmount(), item.getDescription());
    }
}
