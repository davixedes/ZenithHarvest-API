package com.fiap.zenith.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Trilha de acesso (LGPD): registra cada tentativa de login/logout. Domínio Users.
 * {@code UserId} é nullable (uma falha de login pode ocorrer com e-mail inexistente).
 */
@Entity
@Table(name = "AccessLog")
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id")
    private UUID id;

    @Column(name = "UserId")
    private UUID userId;

    @Column(name = "AccessLogActionId", nullable = false)
    private Integer accessLogActionId;

    @Column(name = "Ip", length = 45)
    private String ip;

    @Column(name = "UserAgent", length = 500)
    private String userAgent;

    @Column(name = "Success", nullable = false)
    private Boolean success;

    @Column(name = "FailureReason", length = 200)
    private String failureReason;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected AccessLog() {
        // exigido pelo JPA
    }

    public static AccessLog register(UUID userId, Integer accessLogActionId, boolean success,
                                     String ip, String userAgent, String failureReason) {
        AccessLog log = new AccessLog();
        log.userId = userId;
        log.accessLogActionId = accessLogActionId;
        log.success = success;
        log.ip = ip;
        log.userAgent = userAgent;
        log.failureReason = failureReason;
        log.createdAt = OffsetDateTime.now();
        return log;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public Integer getAccessLogActionId() {
        return accessLogActionId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
