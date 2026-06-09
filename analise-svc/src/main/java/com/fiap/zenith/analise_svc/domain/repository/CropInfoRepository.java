package com.fiap.zenith.analise_svc.domain.repository;

import com.fiap.zenith.analise_svc.domain.entity.CropInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CropInfoRepository extends JpaRepository<CropInfo, UUID> {

    Optional<CropInfo> findByIdAndDeletedAtIsNull(UUID id);
}
