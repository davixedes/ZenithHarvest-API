package com.fiap.zenith.analise_svc.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resultado de análise satelital de um talhão vinculado a um sinistro.
 * Persistido no Postgres compartilhado (schema do core-svc).
 */
@Entity
@Table(name = "SatelliteAnalysis")
public class SatelliteAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "ClaimId")
    private UUID claimId;

    @Column(name = "PlotId", nullable = false)
    private UUID plotId;

    @Column(name = "SatelliteSourceId", nullable = false)
    private Integer satelliteSourceId;

    @Column(name = "SatelliteClassId")
    private Integer satelliteClassId;

    @Column(name = "ImageDate")
    private LocalDate imageDate;

    @Column(name = "SceneId", length = 100)
    private String sceneId;

    @Column(name = "ImageUrl", length = 500)
    private String imageUrl;

    @Column(name = "MeanNdvi", precision = 4, scale = 3)
    private BigDecimal meanNdvi;

    @Column(name = "MeanEvi", precision = 4, scale = 3)
    private BigDecimal meanEvi;

    @Column(name = "CloudCoveragePct", precision = 5, scale = 2)
    private BigDecimal cloudCoveragePct;

    @Column(name = "AffectedAreaM2", precision = 15, scale = 2)
    private BigDecimal affectedAreaM2;

    @Column(name = "MlConfidence", precision = 5, scale = 2)
    private BigDecimal mlConfidence;

    @Column(name = "ProcessedAt")
    private OffsetDateTime processedAt;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "EditedAt")
    private OffsetDateTime editedAt;

    @Column(name = "DeletedAt")
    private OffsetDateTime deletedAt;

    protected SatelliteAnalysis() {}

    public static SatelliteAnalysis create(UUID claimId, UUID plotId, Integer satelliteSourceId,
                                            Integer satelliteClassId, LocalDate imageDate,
                                            BigDecimal meanNdvi, BigDecimal meanEvi,
                                            BigDecimal cloudCoveragePct, BigDecimal affectedAreaM2,
                                            BigDecimal mlConfidence) {
        SatelliteAnalysis sa = new SatelliteAnalysis();
        sa.claimId = claimId;
        sa.plotId = plotId;
        sa.satelliteSourceId = satelliteSourceId;
        sa.satelliteClassId = satelliteClassId;
        sa.imageDate = imageDate;
        sa.meanNdvi = meanNdvi;
        sa.meanEvi = meanEvi;
        sa.cloudCoveragePct = cloudCoveragePct;
        sa.affectedAreaM2 = affectedAreaM2;
        sa.mlConfidence = mlConfidence;
        sa.processedAt = OffsetDateTime.now();
        sa.createdAt = OffsetDateTime.now();
        return sa;
    }

    public UUID getId() { return id; }
    public UUID getClaimId() { return claimId; }
    public UUID getPlotId() { return plotId; }
    public Integer getSatelliteSourceId() { return satelliteSourceId; }
    public Integer getSatelliteClassId() { return satelliteClassId; }
    public LocalDate getImageDate() { return imageDate; }
    public String getSceneId() { return sceneId; }
    public String getImageUrl() { return imageUrl; }
    public BigDecimal getMeanNdvi() { return meanNdvi; }
    public BigDecimal getMeanEvi() { return meanEvi; }
    public BigDecimal getCloudCoveragePct() { return cloudCoveragePct; }
    public BigDecimal getAffectedAreaM2() { return affectedAreaM2; }
    public BigDecimal getMlConfidence() { return mlConfidence; }
    public OffsetDateTime getProcessedAt() { return processedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getEditedAt() { return editedAt; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
}
