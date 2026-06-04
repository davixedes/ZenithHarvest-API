package com.fiap.zenith.core.application.mapper;

import com.fiap.zenith.core.application.dto.CropResponse;
import com.fiap.zenith.core.domain.entity.Crop;
import org.springframework.stereotype.Component;

@Component
public class CropMapper {

    public CropResponse toResponse(Crop c) {
        return new CropResponse(
                c.getId(), c.getCode(), c.getName(), c.getScientificName(),
                c.getAverageCycleDays(), c.getExpectedNdviMin(), c.getExpectedNdviMax(),
                c.getAverageValuePerHectare(), c.getDroughtVulnerability(), c.getFrostVulnerability(),
                c.getStatus(), c.getCreatedAt(), c.getEditedAt());
    }
}
