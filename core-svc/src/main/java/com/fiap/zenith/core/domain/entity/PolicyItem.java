package com.fiap.zenith.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Cobertura de uma apólice para um tipo de evento (Item de composição).
 * Único por (PolicyId, ClaimEventTypeId) — uma apólice não cobre o mesmo evento duas vezes.
 */
@Entity
@Table(name = "PolicyItem")
public class PolicyItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "PolicyId", nullable = false)
    private UUID policyId;

    @Column(name = "ClaimEventTypeId", nullable = false)
    private Integer claimEventTypeId;

    @Column(name = "CoveragePct", nullable = false, precision = 5, scale = 2)
    private BigDecimal coveragePct = new BigDecimal("100");

    @Column(name = "MaxCoverageAmount", precision = 15, scale = 2)
    private BigDecimal maxCoverageAmount;

    @Column(name = "Notes", length = 500)
    private String notes;

    protected PolicyItem() {}

    public static PolicyItem create(UUID policyId, Integer claimEventTypeId,
                                     BigDecimal coveragePct, BigDecimal maxCoverageAmount,
                                     String notes) {
        PolicyItem item = new PolicyItem();
        item.policyId = policyId;
        item.claimEventTypeId = claimEventTypeId;
        item.coveragePct = coveragePct != null ? coveragePct : new BigDecimal("100");
        item.maxCoverageAmount = maxCoverageAmount;
        item.notes = notes;
        return item;
    }

    public UUID getId() { return id; }
    public UUID getPolicyId() { return policyId; }
    public Integer getClaimEventTypeId() { return claimEventTypeId; }
    public BigDecimal getCoveragePct() { return coveragePct; }
    public BigDecimal getMaxCoverageAmount() { return maxCoverageAmount; }
    public String getNotes() { return notes; }

    public void setCoveragePct(BigDecimal coveragePct) { this.coveragePct = coveragePct; }
    public void setMaxCoverageAmount(BigDecimal maxCoverageAmount) { this.maxCoverageAmount = maxCoverageAmount; }
    public void setNotes(String notes) { this.notes = notes; }
}
