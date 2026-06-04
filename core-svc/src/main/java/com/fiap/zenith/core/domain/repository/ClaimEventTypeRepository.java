package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.ClaimEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimEventTypeRepository extends JpaRepository<ClaimEventType, Integer> {
    List<ClaimEventType> findAllByActiveTrue();
}
