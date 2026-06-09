package com.fiap.zenith.analise_svc.domain.repository;

import com.fiap.zenith.analise_svc.domain.entity.PlotInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlotInfoRepository extends JpaRepository<PlotInfo, UUID> {

    Optional<PlotInfo> findByIdAndDeletedAtIsNull(UUID id);
}
