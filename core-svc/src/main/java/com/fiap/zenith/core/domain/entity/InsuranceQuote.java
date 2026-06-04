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

/** Cotação de seguro gerada para um talhão específico. */
@Entity
@Table(name = "InsuranceQuote")
public class InsuranceQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Generated(event = EventType.INSERT)
    @Column(name = "Code", updatable = false, insertable = false)
    private Integer code;

    @Column(name = "UserId", nullable = false)
    private UUID userId;

    @Column(name = "PlotId", nullable = false)
    private UUID plotId;

    @Column(name = "InsuranceId", nullable = false)
    private UUID insuranceId;

    @Column(name = "QuoteSituationId", nullable = false)
    private Integer quoteSituationId;

    @Column(name = "InsuredAmount", precision = 15, scale = 2)
    private BigDecimal insuredAmount;

    @Column(name = "TotalPremium", precision = 15, scale = 2)
    private BigDecimal totalPremium;

    @Column(name = "MonthlyPremium", precision = 15, scale = 2)
    private BigDecimal monthlyPremium;

    @Column(name = "RegionalFactor", precision = 5, scale = 3)
    private BigDecimal regionalFactor;

    @Column(name = "HistoryFactor", precision = 5, scale = 3)
    private BigDecimal historyFactor;

    @Column(name = "ValidUntil")
    private OffsetDateTime validUntil;

    @Column(name = "AcceptedAt")
    private OffsetDateTime acceptedAt;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "EditedAt")
    private OffsetDateTime editedAt;

    @Column(name = "DeletedAt")
    private OffsetDateTime deletedAt;

    protected InsuranceQuote() {}

    public static InsuranceQuote create(UUID userId, UUID plotId, UUID insuranceId,
                                         Integer quoteSituationId, BigDecimal insuredAmount,
                                         BigDecimal totalPremium, BigDecimal monthlyPremium,
                                         BigDecimal regionalFactor, BigDecimal historyFactor,
                                         OffsetDateTime validUntil) {
        InsuranceQuote q = new InsuranceQuote();
        q.userId = userId;
        q.plotId = plotId;
        q.insuranceId = insuranceId;
        q.quoteSituationId = quoteSituationId;
        q.insuredAmount = insuredAmount;
        q.totalPremium = totalPremium;
        q.monthlyPremium = monthlyPremium;
        q.regionalFactor = regionalFactor != null ? regionalFactor : BigDecimal.ONE;
        q.historyFactor = historyFactor != null ? historyFactor : BigDecimal.ONE;
        q.validUntil = validUntil;
        q.createdAt = OffsetDateTime.now();
        return q;
    }

    public UUID getId() { return id; }
    public Integer getCode() { return code; }
    public UUID getUserId() { return userId; }
    public UUID getPlotId() { return plotId; }
    public UUID getInsuranceId() { return insuranceId; }
    public Integer getQuoteSituationId() { return quoteSituationId; }
    public BigDecimal getInsuredAmount() { return insuredAmount; }
    public BigDecimal getTotalPremium() { return totalPremium; }
    public BigDecimal getMonthlyPremium() { return monthlyPremium; }
    public BigDecimal getRegionalFactor() { return regionalFactor; }
    public BigDecimal getHistoryFactor() { return historyFactor; }
    public OffsetDateTime getValidUntil() { return validUntil; }
    public OffsetDateTime getAcceptedAt() { return acceptedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getEditedAt() { return editedAt; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }

    public void setQuoteSituationId(Integer quoteSituationId) { this.quoteSituationId = quoteSituationId; }
    public void setAcceptedAt(OffsetDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    public void setEditedAt(OffsetDateTime editedAt) { this.editedAt = editedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
}
