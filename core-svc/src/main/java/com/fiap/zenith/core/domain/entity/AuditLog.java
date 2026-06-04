package com.fiap.zenith.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Auditoria cross-cutting (LGPD): LOGIN/LOGOUT/ANONYMIZATION/BLOCK e operações
 * sensíveis. {@code TargetEntityId} é VARCHAR polimórfico (UUID de entidade ou
 * Id de lookup); {@code DescriptionJson} é JSONB indexável.
 * Imutável após gravação — sem setters de negócio.
 */
@Entity
@Table(name = "AuditLog")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "UserId")
    private UUID userId;

    @Column(name = "SystemActor", length = 50)
    private String systemActor;

    @Column(name = "Operation", nullable = false, length = 30)
    private String operation;

    @Column(name = "TargetEntity", length = 50)
    private String targetEntity;

    @Column(name = "TargetEntityId", length = 64)
    private String targetEntityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "DescriptionJson", columnDefinition = "jsonb")
    private String descriptionJson;

    @Column(name = "Ip", length = 45)
    private String ip;

    @Column(name = "UserAgent", length = 500)
    private String userAgent;

    @Column(name = "RequestId")
    private UUID requestId;

    @Column(name = "Success", nullable = false)
    private Boolean success = true;

    @Column(name = "FailureReason", length = 500)
    private String failureReason;

    @Column(name = "OperatedAt", nullable = false, updatable = false)
    private OffsetDateTime operatedAt;

    protected AuditLog() {}

    public static AuditLog registrar(UUID userId, String systemActor, String operation,
                                      String targetEntity, String targetEntityId,
                                      String descriptionJson, String ip, String userAgent,
                                      boolean success, String failureReason) {
        AuditLog log = new AuditLog();
        log.userId = userId;
        log.systemActor = systemActor;
        log.operation = operation;
        log.targetEntity = targetEntity;
        log.targetEntityId = targetEntityId;
        log.descriptionJson = descriptionJson;
        log.ip = ip;
        log.userAgent = userAgent;
        log.success = success;
        log.failureReason = failureReason;
        log.operatedAt = OffsetDateTime.now();
        return log;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getSystemActor() { return systemActor; }
    public String getOperation() { return operation; }
    public String getTargetEntity() { return targetEntity; }
    public String getTargetEntityId() { return targetEntityId; }
    public String getDescriptionJson() { return descriptionJson; }
    public String getIp() { return ip; }
    public String getUserAgent() { return userAgent; }
    public UUID getRequestId() { return requestId; }
    public Boolean getSuccess() { return success; }
    public String getFailureReason() { return failureReason; }
    public OffsetDateTime getOperatedAt() { return operatedAt; }
}
