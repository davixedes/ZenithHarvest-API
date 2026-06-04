package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.Biome;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BiomeRepository extends JpaRepository<Biome, Integer> {
    List<Biome> findAllByActiveTrue();
}
