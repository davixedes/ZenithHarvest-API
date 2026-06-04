package com.fiap.zenith.core.application.mapper;

import com.fiap.zenith.core.application.dto.PreventiveAlertResponse;
import com.fiap.zenith.core.domain.entity.PreventiveAlert;
import org.springframework.stereotype.Component;

@Component
public class PreventiveAlertMapper {
    public PreventiveAlertResponse toResponse(PreventiveAlert alert) {
        return new PreventiveAlertResponse(alert.getId(), alert.getPlotId(), alert.getAlertTypeId(),
                alert.getAlertSeverityId(), alert.getAlertSituationId(), alert.getMessage(),
                alert.getObservedNdvi(), alert.getExpectedNdvi(), alert.getDropPct(),
                alert.getIssuedAt(), alert.getViewedAt(), alert.getCreatedAt());
    }
}
