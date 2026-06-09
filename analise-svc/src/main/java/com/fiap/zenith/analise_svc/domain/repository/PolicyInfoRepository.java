package com.fiap.zenith.analise_svc.domain.repository;

import com.fiap.zenith.analise_svc.domain.entity.PolicyInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PolicyInfoRepository extends JpaRepository<PolicyInfo, UUID> {

    /**
     * Retorna todos os plotIds com apólice vigente (situação 1 = Vigente no seed).
     * StartDate <= hoje <= EndDate, sem soft delete.
     */
    @Query("""
            select p.plotId from PolicyInfo p
            where p.policySituationId = 1
              and p.startDate <= :hoje
              and p.endDate >= :hoje
              and p.deletedAt is null
            """)
    List<UUID> findPlotIdsComApoliceVigente(@Param("hoje") LocalDate hoje);
}
