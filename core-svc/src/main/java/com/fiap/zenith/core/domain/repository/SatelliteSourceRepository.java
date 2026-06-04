package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.SatelliteSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SatelliteSourceRepository extends JpaRepository<SatelliteSource, Integer> {
    List<SatelliteSource> findAllByActiveTrue();
}
