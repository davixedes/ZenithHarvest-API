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

/**
 * Sinistro — registro de perda agrícola vinculado a uma apólice.
 * Ao ser criado, o core-svc publica {@code sinistro.aberto} no RabbitMQ para o analise-svc.
 */
@Entity
@Table(name = "Claim")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Generated(event = EventType.INSERT)
    @Column(name = "Code", updatable = false, insertable = false)
    private Integer code;

    @Column(name = "ClaimNumber", unique = true, length = 30)
    private String claimNumber;

    @Column(name = "PolicyId", nullable = false)
    private UUID policyId;

    @Column(name = "ClaimSituationId", nullable = false)
    private Integer claimSituationId;

    @Column(name = "CategoryId", nullable = false)
    private Integer categoryId;

    @Column(name = "SubCategoryId", nullable = false)
    private Integer subCategoryId;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "PhotoUrl", length = 500)
    private String photoUrl;

    @Column(name = "OpeningGpsLat", precision = 10, scale = 7)
    private BigDecimal openingGpsLat;

    @Column(name = "OpeningGpsLng", precision = 10, scale = 7)
    private BigDecimal openingGpsLng;

    @Column(name = "NdviBefore", precision = 4, scale = 3)
    private BigDecimal ndviBefore;

    @Column(name = "NdviAfter", precision = 4, scale = 3)
    private BigDecimal ndviAfter;

    @Column(name = "TotalLossPct", precision = 5, scale = 2)
    private BigDecimal totalLossPct;

    @Column(name = "TotalAffectedAreaHa", precision = 10, scale = 2)
    private BigDecimal totalAffectedAreaHa;

    @Column(name = "CalculatedAmount", precision = 15, scale = 2)
    private BigDecimal calculatedAmount;

    @Column(name = "ApprovedAmount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "MlConfidenceScore", precision = 5, scale = 2)
    private BigDecimal mlConfidenceScore;

    @Column(name = "FraudFlag")
    private Boolean fraudFlag;

    @Column(name = "RejectionReasonId")
    private Integer rejectionReasonId;

    @Column(name = "AnalystId")
    private UUID analystId;

    @Column(name = "ApprovedAt")
    private OffsetDateTime approvedAt;

    @Column(name = "PaidAt")
    private OffsetDateTime paidAt;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "EditedAt")
    private OffsetDateTime editedAt;

    @Column(name = "DeletedAt")
    private OffsetDateTime deletedAt;

    protected Claim() {}

    public static Claim create(String claimNumber, UUID policyId, Integer claimSituationId,
                                Integer categoryId, Integer subCategoryId, String description,
                                String photoUrl, BigDecimal openingGpsLat, BigDecimal openingGpsLng,
                                BigDecimal ndviBefore) {
        Claim c = new Claim();
        c.claimNumber = claimNumber;
        c.policyId = policyId;
        c.claimSituationId = claimSituationId;
        c.categoryId = categoryId;
        c.subCategoryId = subCategoryId;
        c.description = description;
        c.photoUrl = photoUrl;
        c.openingGpsLat = openingGpsLat;
        c.openingGpsLng = openingGpsLng;
        c.ndviBefore = ndviBefore;
        c.fraudFlag = false; // operacional: nasce sem flag; a IA decide depois (evita NOT NULL no INSERT)
        c.createdAt = OffsetDateTime.now();
        return c;
    }

    public UUID getId() { return id; }
    public Integer getCode() { return code; }
    public String getClaimNumber() { return claimNumber; }
    public UUID getPolicyId() { return policyId; }
    public Integer getClaimSituationId() { return claimSituationId; }
    public Integer getCategoryId() { return categoryId; }
    public Integer getSubCategoryId() { return subCategoryId; }
    public String getDescription() { return description; }
    public String getPhotoUrl() { return photoUrl; }
    public BigDecimal getOpeningGpsLat() { return openingGpsLat; }
    public BigDecimal getOpeningGpsLng() { return openingGpsLng; }
    public BigDecimal getNdviBefore() { return ndviBefore; }
    public BigDecimal getNdviAfter() { return ndviAfter; }
    public BigDecimal getTotalLossPct() { return totalLossPct; }
    public BigDecimal getTotalAffectedAreaHa() { return totalAffectedAreaHa; }
    public BigDecimal getCalculatedAmount() { return calculatedAmount; }
    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public BigDecimal getMlConfidenceScore() { return mlConfidenceScore; }
    public Boolean getFraudFlag() { return fraudFlag; }
    public Integer getRejectionReasonId() { return rejectionReasonId; }
    public UUID getAnalystId() { return analystId; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public OffsetDateTime getPaidAt() { return paidAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getEditedAt() { return editedAt; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }

    public void setClaimSituationId(Integer claimSituationId) { this.claimSituationId = claimSituationId; }
    public void setNdviAfter(BigDecimal ndviAfter) { this.ndviAfter = ndviAfter; }
    public void setTotalLossPct(BigDecimal totalLossPct) { this.totalLossPct = totalLossPct; }
    public void setTotalAffectedAreaHa(BigDecimal totalAffectedAreaHa) { this.totalAffectedAreaHa = totalAffectedAreaHa; }
    public void setCalculatedAmount(BigDecimal calculatedAmount) { this.calculatedAmount = calculatedAmount; }
    public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }
    public void setMlConfidenceScore(BigDecimal mlConfidenceScore) { this.mlConfidenceScore = mlConfidenceScore; }
    public void setFraudFlag(Boolean fraudFlag) { this.fraudFlag = fraudFlag; }
    public void setRejectionReasonId(Integer rejectionReasonId) { this.rejectionReasonId = rejectionReasonId; }
    public void setAnalystId(UUID analystId) { this.analystId = analystId; }
    public void setApprovedAt(OffsetDateTime approvedAt) { this.approvedAt = approvedAt; }
    public void setPaidAt(OffsetDateTime paidAt) { this.paidAt = paidAt; }
    public void setEditedAt(OffsetDateTime editedAt) { this.editedAt = editedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
}
