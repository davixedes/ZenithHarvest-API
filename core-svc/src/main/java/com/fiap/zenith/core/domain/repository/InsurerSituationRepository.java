package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.InsurerSituation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InsurerSituationRepository extends JpaRepository<InsurerSituation, Integer> {
    List<InsurerSituation> findAllByActiveTrue();
}
