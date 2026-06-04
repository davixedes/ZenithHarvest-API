package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.AlertSeverity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertSeverityRepository extends JpaRepository<AlertSeverity, Integer> {
    List<AlertSeverity> findAll();
}
