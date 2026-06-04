package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.PolicyItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.util.UUID;

public interface PolicyItemRepository extends JpaRepository<PolicyItem, UUID> {
    @Query("""
            select pi from PolicyItem pi
            where exists (
                select 1 from Policy p
                where p.id = pi.policyId and p.deletedAt is null
            )
            """)
    Page<PolicyItem> findAllWithActivePolicy(Pageable pageable);

    @Query("""
            select pi from PolicyItem pi
            where pi.policyId = :policyId
              and exists (
                select 1 from Policy p
                where p.id = pi.policyId and p.deletedAt is null
            )
            """)
    Page<PolicyItem> findAllByPolicyIdWithActivePolicy(@Param("policyId") UUID policyId, Pageable pageable);

    @Query("""
            select pi from PolicyItem pi
            where pi.id = :id
              and exists (
                select 1 from Policy p
                where p.id = pi.policyId and p.deletedAt is null
            )
            """)
    Optional<PolicyItem> findAccessibleById(@Param("id") UUID id);

    boolean existsByPolicyIdAndClaimEventTypeId(UUID policyId, Integer claimEventTypeId);
}
