package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.RejectionReason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RejectionReasonRepository extends JpaRepository<RejectionReason, Integer> {
    List<RejectionReason> findAllByActiveTrue();
}
