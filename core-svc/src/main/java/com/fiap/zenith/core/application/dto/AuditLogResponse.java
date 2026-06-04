package com.fiap.zenith.core.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID userId,
        String systemActor,
        String operation,
        String targetEntity,
        String targetEntityId,
        String descriptionJson,
        String ip,
        String userAgent,
        UUID requestId,
        Boolean success,
        String failureReason,
        OffsetDateTime operatedAt
) {}
