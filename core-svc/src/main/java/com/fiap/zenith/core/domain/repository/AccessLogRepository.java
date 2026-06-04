package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data para {@link AccessLog} (trilha de acesso / LGPD).
 */
public interface AccessLogRepository extends JpaRepository<AccessLog, UUID> {
}
