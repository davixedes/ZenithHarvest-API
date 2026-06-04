package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.ClaimSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimSubCategoryRepository extends JpaRepository<ClaimSubCategory, Integer> {
    List<ClaimSubCategory> findAllByActiveTrue();
}
