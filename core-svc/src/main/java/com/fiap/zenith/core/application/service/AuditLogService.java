package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.AuditLogResponse;
import com.fiap.zenith.core.application.mapper.AuditLogMapper;
import com.fiap.zenith.core.domain.entity.AuditLog;
import com.fiap.zenith.core.domain.repository.AuditLogRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    public AuditLogService(AuditLogRepository auditLogRepository, AuditLogMapper auditLogMapper) {
        this.auditLogRepository = auditLogRepository;
        this.auditLogMapper = auditLogMapper;
    }

    @Transactional(readOnly = true)
    public AuditLogResponse buscarPorId(UUID id) {
        return auditLogMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> listar(UUID userId, Pageable pageable) {
        Page<AuditLog> page = userId != null
                ? auditLogRepository.findAllByUserId(userId, pageable)
                : auditLogRepository.findAll(pageable);
        return page.map(auditLogMapper::toResponse);
    }

    private AuditLog buscarEntidade(UUID id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Audit log não encontrado: " + id));
    }
}
