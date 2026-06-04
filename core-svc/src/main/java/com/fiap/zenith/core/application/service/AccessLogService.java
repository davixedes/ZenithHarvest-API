package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.domain.entity.AccessLog;
import com.fiap.zenith.core.domain.repository.AccessLogActionRepository;
import com.fiap.zenith.core.domain.repository.AccessLogRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Grava a trilha de acesso (LGPD). Roda em transação própria ({@code REQUIRES_NEW}) para que
 * o registro de uma falha de login persista mesmo quando a transação do login é revertida.
 * Se a ação não estiver no seed do lookup, o registro é silenciosamente ignorado.
 */
@Service
public class AccessLogService {

    private final AccessLogRepository accessLogRepository;
    private final AccessLogActionRepository accessLogActionRepository;

    public AccessLogService(AccessLogRepository accessLogRepository,
                            AccessLogActionRepository accessLogActionRepository) {
        this.accessLogRepository = accessLogRepository;
        this.accessLogActionRepository = accessLogActionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID userId, String action, boolean success,
                       String ip, String userAgent, String failureReason) {
        accessLogActionRepository.findByDescriptionAndActiveTrue(action).ifPresent(a ->
                accessLogRepository.save(
                        AccessLog.register(userId, a.getId(), success, ip, userAgent, failureReason)));
    }
}
