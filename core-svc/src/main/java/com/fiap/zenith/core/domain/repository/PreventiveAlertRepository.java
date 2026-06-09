package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.PreventiveAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PreventiveAlertRepository extends JpaRepository<PreventiveAlert, UUID> {
    @Query("""
            select pa from PreventiveAlert pa
            where exists (
                select 1 from Plot p
                where p.id = pa.plotId and p.deletedAt is null
            )
            """)
    Page<PreventiveAlert> findAllWithActivePlot(Pageable pageable);

    @Query("""
            select pa from PreventiveAlert pa
            where pa.plotId = :plotId
              and exists (
                select 1 from Plot p
                where p.id = pa.plotId and p.deletedAt is null
            )
            """)
    Page<PreventiveAlert> findAllByPlotIdWithActivePlot(@Param("plotId") UUID plotId, Pageable pageable);

    @Query("""
            select pa from PreventiveAlert pa
            where pa.id = :id
              and exists (
                select 1 from Plot p
                where p.id = pa.plotId and p.deletedAt is null
            )
            """)
    Optional<PreventiveAlert> findAccessibleById(@Param("id") UUID id);

    boolean existsByPlotIdAndAlertTypeIdAndAlertSituationIdIn(
            UUID plotId, Integer alertTypeId, java.util.List<Integer> situationIds);
}
