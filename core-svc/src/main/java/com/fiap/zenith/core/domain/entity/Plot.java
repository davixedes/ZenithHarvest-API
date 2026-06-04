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

/**
 * Talhão: subdivisão de uma fazenda onde é cultivada uma cultura específica.
 * Unidade mínima de análise satelital e cobertura de sinistro.
 */
@Entity
@Table(name = "Plot")
public class Plot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Generated(event = EventType.INSERT)
    @Column(name = "Code", updatable = false, insertable = false)
    private Integer code;

    @Column(name = "FarmId", nullable = false)
    private UUID farmId;

    @Column(name = "CropId")
    private UUID cropId;

    @Column(name = "PlotSituationId", nullable = false)
    private Integer plotSituationId;

    @Column(name = "ProductionSystemId")
    private Integer productionSystemId;

    @Column(name = "Identifier", length = 40)
    private String identifier;

    @Column(name = "AreaHectares", precision = 10, scale = 2)
    private BigDecimal areaHectares;

    @Column(name = "PlantingDate")
    private LocalDate plantingDate;

    @Column(name = "EstimatedHarvestDate")
    private LocalDate estimatedHarvestDate;

    @Column(name = "CycleDays")
    private Integer cycleDays;

    @Column(name = "SeedVariety", length = 100)
    private String seedVariety;

    @Column(name = "PolygonWkt", columnDefinition = "TEXT")
    private String polygonWkt;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "EditedAt")
    private OffsetDateTime editedAt;

    @Column(name = "DeletedAt")
    private OffsetDateTime deletedAt;

    protected Plot() {}

    public static Plot create(UUID farmId, UUID cropId, Integer plotSituationId,
                              Integer productionSystemId, String identifier,
                              BigDecimal areaHectares, LocalDate plantingDate,
                              LocalDate estimatedHarvestDate, Integer cycleDays,
                              String seedVariety, String polygonWkt) {
        Plot p = new Plot();
        p.farmId = farmId;
        p.cropId = cropId;
        p.plotSituationId = plotSituationId;
        p.productionSystemId = productionSystemId;
        p.identifier = identifier;
        p.areaHectares = areaHectares;
        p.plantingDate = plantingDate;
        p.estimatedHarvestDate = estimatedHarvestDate;
        p.cycleDays = cycleDays;
        p.seedVariety = seedVariety;
        p.polygonWkt = polygonWkt;
        p.createdAt = OffsetDateTime.now();
        return p;
    }

    public UUID getId() { return id; }
    public Integer getCode() { return code; }
    public UUID getFarmId() { return farmId; }
    public UUID getCropId() { return cropId; }
    public Integer getPlotSituationId() { return plotSituationId; }
    public Integer getProductionSystemId() { return productionSystemId; }
    public String getIdentifier() { return identifier; }
    public BigDecimal getAreaHectares() { return areaHectares; }
    public LocalDate getPlantingDate() { return plantingDate; }
    public LocalDate getEstimatedHarvestDate() { return estimatedHarvestDate; }
    public Integer getCycleDays() { return cycleDays; }
    public String getSeedVariety() { return seedVariety; }
    public String getPolygonWkt() { return polygonWkt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getEditedAt() { return editedAt; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }

    public void setCropId(UUID cropId) { this.cropId = cropId; }
    public void setPlotSituationId(Integer plotSituationId) { this.plotSituationId = plotSituationId; }
    public void setProductionSystemId(Integer productionSystemId) { this.productionSystemId = productionSystemId; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public void setAreaHectares(BigDecimal areaHectares) { this.areaHectares = areaHectares; }
    public void setPlantingDate(LocalDate plantingDate) { this.plantingDate = plantingDate; }
    public void setEstimatedHarvestDate(LocalDate estimatedHarvestDate) { this.estimatedHarvestDate = estimatedHarvestDate; }
    public void setCycleDays(Integer cycleDays) { this.cycleDays = cycleDays; }
    public void setSeedVariety(String seedVariety) { this.seedVariety = seedVariety; }
    public void setPolygonWkt(String polygonWkt) { this.polygonWkt = polygonWkt; }
    public void setEditedAt(OffsetDateTime editedAt) { this.editedAt = editedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
}
