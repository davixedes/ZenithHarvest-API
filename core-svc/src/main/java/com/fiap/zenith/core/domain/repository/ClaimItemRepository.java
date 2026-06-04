package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.ClaimItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.util.UUID;

public interface ClaimItemRepository extends JpaRepository<ClaimItem, UUID> {
    @Query("""
            select ci from ClaimItem ci
            where exists (
                select 1 from Claim c
                where c.id = ci.claimId and c.deletedAt is null
            )
            """)
    Page<ClaimItem> findAllWithActiveClaim(Pageable pageable);

    @Query("""
            select ci from ClaimItem ci
            where ci.claimId = :claimId
              and exists (
                select 1 from Claim c
                where c.id = ci.claimId and c.deletedAt is null
            )
            """)
    Page<ClaimItem> findAllByClaimIdWithActiveClaim(@Param("claimId") UUID claimId, Pageable pageable);

    @Query("""
            select ci from ClaimItem ci
            where ci.id = :id
              and exists (
                select 1 from Claim c
                where c.id = ci.claimId and c.deletedAt is null
            )
            """)
    Optional<ClaimItem> findAccessibleById(@Param("id") UUID id);
}
