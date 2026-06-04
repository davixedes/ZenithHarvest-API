package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.ClaimCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimCategoryRepository extends JpaRepository<ClaimCategory, Integer> {
    List<ClaimCategory> findAllByActiveTrue();
}
