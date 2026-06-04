package com.fiap.zenith.core.application.mapper;

import com.fiap.zenith.core.application.dto.InsurerResponse;
import com.fiap.zenith.core.domain.entity.Insurer;
import org.springframework.stereotype.Component;

@Component
public class InsurerMapper {
    public InsurerResponse toResponse(Insurer i) {
        return new InsurerResponse(i.getId(), i.getCode(), i.getCorporateName(), i.getTradeName(),
                i.getCnpj(), i.getSusepCode(), i.getCommercialEmail(), i.getPhone(), i.getLogoUrl(),
                i.getAdminFeePct(), i.getTakeRatePct(), i.getActive(), i.getInsurerSituationId(),
                i.getAccreditedAt(), i.getCreatedAt(), i.getEditedAt());
    }
}
