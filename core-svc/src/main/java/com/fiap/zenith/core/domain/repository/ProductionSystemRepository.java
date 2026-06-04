package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.ProductionSystem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductionSystemRepository extends JpaRepository<ProductionSystem, Integer> {
    List<ProductionSystem> findAllByActiveTrue();
}
