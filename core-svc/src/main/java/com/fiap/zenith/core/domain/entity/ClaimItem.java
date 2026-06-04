package com.fiap.zenith.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/** Detalhamento de um sinistro por tipo de evento (Item de composição). */
@Entity
@Table(name = "ClaimItem")
public class ClaimItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "ClaimId", nullable = false)
    private UUID claimId;

    @Column(name = "ClaimEventTypeId", nullable = false)
    private Integer claimEventTypeId;

    @Column(name = "AffectedAreaHa", precision = 10, scale = 2)
    private BigDecimal affectedAreaHa;

    @Column(name = "LossPct", precision = 5, scale = 2)
    private BigDecimal lossPct;

    @Column(name = "NdviBefore", precision = 4, scale = 3)
    private BigDecimal ndviBefore;

    @Column(name = "NdviAfter", precision = 4, scale = 3)
    private BigDecimal ndviAfter;

    @Column(name = "ItemAmount", precision = 15, scale = 2)
    private BigDecimal itemAmount;

    @Column(name = "Description", length = 500)
    private String description;

    protected ClaimItem() {}

    public static ClaimItem create(UUID claimId, Integer claimEventTypeId,
                                    BigDecimal affectedAreaHa, BigDecimal lossPct,
                                    BigDecimal ndviBefore, BigDecimal ndviAfter,
                                    BigDecimal itemAmount, String description) {
        ClaimItem item = new ClaimItem();
        item.claimId = claimId;
        item.claimEventTypeId = claimEventTypeId;
        item.affectedAreaHa = affectedAreaHa;
        item.lossPct = lossPct;
        item.ndviBefore = ndviBefore;
        item.ndviAfter = ndviAfter;
        item.itemAmount = itemAmount;
        item.description = description;
        return item;
    }

    public UUID getId() { return id; }
    public UUID getClaimId() { return claimId; }
    public Integer getClaimEventTypeId() { return claimEventTypeId; }
    public BigDecimal getAffectedAreaHa() { return affectedAreaHa; }
    public BigDecimal getLossPct() { return lossPct; }
    public BigDecimal getNdviBefore() { return ndviBefore; }
    public BigDecimal getNdviAfter() { return ndviAfter; }
    public BigDecimal getItemAmount() { return itemAmount; }
    public String getDescription() { return description; }

    public void setAffectedAreaHa(BigDecimal affectedAreaHa) { this.affectedAreaHa = affectedAreaHa; }
    public void setLossPct(BigDecimal lossPct) { this.lossPct = lossPct; }
    public void setNdviBefore(BigDecimal ndviBefore) { this.ndviBefore = ndviBefore; }
    public void setNdviAfter(BigDecimal ndviAfter) { this.ndviAfter = ndviAfter; }
    public void setItemAmount(BigDecimal itemAmount) { this.itemAmount = itemAmount; }
    public void setDescription(String description) { this.description = description; }
}
