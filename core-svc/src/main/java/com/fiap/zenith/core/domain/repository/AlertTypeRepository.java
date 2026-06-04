package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertTypeRepository extends JpaRepository<AlertType, Integer> {
    List<AlertType> findAllByActiveTrue();
}
