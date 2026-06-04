package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.AccessLogAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data para o lookup {@link AccessLogAction}. Resolve o Id da ação pela descrição
 * (ex.: "LOGIN_SUCCESS") na hora de gravar a trilha de acesso.
 */
public interface AccessLogActionRepository extends JpaRepository<AccessLogAction, Integer> {

    Optional<AccessLogAction> findByDescriptionAndActiveTrue(String description);
}
