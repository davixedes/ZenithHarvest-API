package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.SatelliteClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SatelliteClassRepository extends JpaRepository<SatelliteClass, Integer> {
    List<SatelliteClass> findAllByActiveTrue();
}
