package com.fiap.zenith.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Produto de seguro paramétrico oferecido por uma seguradora. */
@Entity
@Table(name = "Insurance")
public class Insurance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Generated(event = EventType.INSERT)
    @Column(name = "Code", updatable = false, insertable = false)
    private Integer code;

    @Column(name = "InsurerId", nullable = false)
    private UUID insurerId;

    @Column(name = "Name", nullable = false, length = 150)
    private String name;

    @Column(name = "Description", length = 500)
    private String description;

    @Column(name = "DeductiblePct", precision = 5, scale = 2)
    private BigDecimal deductiblePct;

    @Column(name = "GraceDays")
    private Integer graceDays;

    @Column(name = "MaxCoveragePerHectare", precision = 15, scale = 2)
    private BigDecimal maxCoveragePerHectare;

    @Column(name = "BaseRatePct", precision = 5, scale = 3)
    private BigDecimal baseRatePct;

    @Column(name = "AvailableStates", length = 100)
    private String availableStates;

    @Column(name = "InsuranceSituationId", nullable = false)
    private Integer insuranceSituationId;

    @Column(name = "Active", nullable = false)
    private Boolean active = true;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "EditedAt")
    private OffsetDateTime editedAt;

    @Column(name = "DeletedAt")
    private OffsetDateTime deletedAt;

    protected Insurance() {}

    public static Insurance create(UUID insurerId, String name, String description,
                                    BigDecimal deductiblePct, Integer graceDays,
                                    BigDecimal maxCoveragePerHectare, BigDecimal baseRatePct,
                                    String availableStates, Integer insuranceSituationId) {
        Insurance ins = new Insurance();
        ins.insurerId = insurerId;
        ins.name = name;
        ins.description = description;
        ins.deductiblePct = deductiblePct != null ? deductiblePct : new BigDecimal("10.00");
        ins.graceDays = graceDays != null ? graceDays : 7;
        ins.maxCoveragePerHectare = maxCoveragePerHectare;
        ins.baseRatePct = baseRatePct;
        ins.availableStates = availableStates;
        ins.insuranceSituationId = insuranceSituationId;
        ins.active = true;
        ins.createdAt = OffsetDateTime.now();
        return ins;
    }

    public UUID getId() { return id; }
    public Integer getCode() { return code; }
    public UUID getInsurerId() { return insurerId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getDeductiblePct() { return deductiblePct; }
    public Integer getGraceDays() { return graceDays; }
    public BigDecimal getMaxCoveragePerHectare() { return maxCoveragePerHectare; }
    public BigDecimal getBaseRatePct() { return baseRatePct; }
    public String getAvailableStates() { return availableStates; }
    public Integer getInsuranceSituationId() { return insuranceSituationId; }
    public Boolean getActive() { return active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getEditedAt() { return editedAt; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setDeductiblePct(BigDecimal deductiblePct) { this.deductiblePct = deductiblePct; }
    public void setGraceDays(Integer graceDays) { this.graceDays = graceDays; }
    public void setMaxCoveragePerHectare(BigDecimal maxCoveragePerHectare) { this.maxCoveragePerHectare = maxCoveragePerHectare; }
    public void setBaseRatePct(BigDecimal baseRatePct) { this.baseRatePct = baseRatePct; }
    public void setAvailableStates(String availableStates) { this.availableStates = availableStates; }
    public void setInsuranceSituationId(Integer insuranceSituationId) { this.insuranceSituationId = insuranceSituationId; }
    public void setActive(Boolean active) { this.active = active; }
    public void setEditedAt(OffsetDateTime editedAt) { this.editedAt = editedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
}
