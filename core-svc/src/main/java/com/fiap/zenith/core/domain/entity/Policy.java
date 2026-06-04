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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Apólice de seguro paramétrico — une cotação aprovada, talhão, seguradora e produto. */
@Entity
@Table(name = "Policy")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Generated(event = EventType.INSERT)
    @Column(name = "Code", updatable = false, insertable = false)
    private Integer code;

    @Column(name = "PolicyNumber", unique = true, length = 30)
    private String policyNumber;

    @Column(name = "InsuranceQuoteId")
    private UUID insuranceQuoteId;

    @Column(name = "PlotId", nullable = false)
    private UUID plotId;

    @Column(name = "InsurerId", nullable = false)
    private UUID insurerId;

    @Column(name = "InsuranceId", nullable = false)
    private UUID insuranceId;

    @Column(name = "PolicySituationId", nullable = false)
    private Integer policySituationId;

    @Column(name = "InsuredAmount", precision = 15, scale = 2)
    private BigDecimal insuredAmount;

    @Column(name = "TotalPremium", precision = 15, scale = 2)
    private BigDecimal totalPremium;

    @Column(name = "MonthlyPremium", precision = 15, scale = 2)
    private BigDecimal monthlyPremium;

    @Column(name = "DeductiblePct", precision = 5, scale = 2)
    private BigDecimal deductiblePct;

    @Column(name = "MaxCoverage", precision = 15, scale = 2)
    private BigDecimal maxCoverage;

    @Column(name = "AccumulatedPaid", precision = 15, scale = 2)
    private BigDecimal accumulatedPaid = BigDecimal.ZERO;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate;

    @Column(name = "ContractedAt")
    private OffsetDateTime contractedAt;

    @Column(name = "CancelledAt")
    private OffsetDateTime cancelledAt;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "EditedAt")
    private OffsetDateTime editedAt;

    @Column(name = "DeletedAt")
    private OffsetDateTime deletedAt;

    protected Policy() {}

    public static Policy create(String policyNumber, UUID insuranceQuoteId, UUID plotId,
                                 UUID insurerId, UUID insuranceId, Integer policySituationId,
                                 BigDecimal insuredAmount, BigDecimal totalPremium,
                                 BigDecimal monthlyPremium, BigDecimal deductiblePct,
                                 BigDecimal maxCoverage, LocalDate startDate, LocalDate endDate) {
        Policy p = new Policy();
        p.policyNumber = policyNumber;
        p.insuranceQuoteId = insuranceQuoteId;
        p.plotId = plotId;
        p.insurerId = insurerId;
        p.insuranceId = insuranceId;
        p.policySituationId = policySituationId;
        p.insuredAmount = insuredAmount;
        p.totalPremium = totalPremium;
        p.monthlyPremium = monthlyPremium;
        p.deductiblePct = deductiblePct;
        p.maxCoverage = maxCoverage;
        p.accumulatedPaid = BigDecimal.ZERO;
        p.startDate = startDate;
        p.endDate = endDate;
        p.contractedAt = OffsetDateTime.now();
        p.createdAt = OffsetDateTime.now();
        return p;
    }

    public UUID getId() { return id; }
    public Integer getCode() { return code; }
    public String getPolicyNumber() { return policyNumber; }
    public UUID getInsuranceQuoteId() { return insuranceQuoteId; }
    public UUID getPlotId() { return plotId; }
    public UUID getInsurerId() { return insurerId; }
    public UUID getInsuranceId() { return insuranceId; }
    public Integer getPolicySituationId() { return policySituationId; }
    public BigDecimal getInsuredAmount() { return insuredAmount; }
    public BigDecimal getTotalPremium() { return totalPremium; }
    public BigDecimal getMonthlyPremium() { return monthlyPremium; }
    public BigDecimal getDeductiblePct() { return deductiblePct; }
    public BigDecimal getMaxCoverage() { return maxCoverage; }
    public BigDecimal getAccumulatedPaid() { return accumulatedPaid; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public OffsetDateTime getContractedAt() { return contractedAt; }
    public OffsetDateTime getCancelledAt() { return cancelledAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getEditedAt() { return editedAt; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }

    public void setPolicySituationId(Integer policySituationId) { this.policySituationId = policySituationId; }
    public void setAccumulatedPaid(BigDecimal accumulatedPaid) { this.accumulatedPaid = accumulatedPaid; }
    public void setCancelledAt(OffsetDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public void setEditedAt(OffsetDateTime editedAt) { this.editedAt = editedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
}
