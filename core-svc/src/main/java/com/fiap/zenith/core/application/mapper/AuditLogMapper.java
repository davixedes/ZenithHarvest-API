package com.fiap.zenith.core.application.mapper;

import com.fiap.zenith.core.application.dto.AuditLogResponse;
import com.fiap.zenith.core.domain.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {
    public AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getUserId(), log.getSystemActor(), log.getOperation(),
                log.getTargetEntity(), log.getTargetEntityId(), log.getDescriptionJson(),
                log.getIp(), log.getUserAgent(), log.getRequestId(), log.getSuccess(),
                log.getFailureReason(), log.getOperatedAt());
    }
}
