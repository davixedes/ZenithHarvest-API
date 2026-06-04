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
 * Cultura agrícola (soja, milho, etc.) com parâmetros de vulnerabilidade e NDVI esperado.
 * Entidade operacional: tem {@code Code} incrementado para exibição no front.
 */
@Entity
@Table(name = "Crop")
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Generated(event = EventType.INSERT)
    @Column(name = "Code", updatable = false, insertable = false)
    private Integer code;

    @Column(name = "Name", nullable = false, length = 80)
    private String name;

    @Column(name = "ScientificName", length = 120)
    private String scientificName;

    @Column(name = "AverageCycleDays")
    private Integer averageCycleDays;

    @Column(name = "ExpectedNdviMin", precision = 4, scale = 3)
    private BigDecimal expectedNdviMin;

    @Column(name = "ExpectedNdviMax", precision = 4, scale = 3)
    private BigDecimal expectedNdviMax;

    @Column(name = "AverageValuePerHectare", precision = 15, scale = 2)
    private BigDecimal averageValuePerHectare;

    @Column(name = "DroughtVulnerability", precision = 3, scale = 1)
    private BigDecimal droughtVulnerability;

    @Column(name = "FrostVulnerability", precision = 3, scale = 1)
    private BigDecimal frostVulnerability;

    @Column(name = "Status", nullable = false)
    private Boolean status = true;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "EditedAt")
    private OffsetDateTime editedAt;

    @Column(name = "DeletedAt")
    private OffsetDateTime deletedAt;

    protected Crop() {}

    public static Crop create(String name, String scientificName, Integer averageCycleDays,
                              BigDecimal expectedNdviMin, BigDecimal expectedNdviMax,
                              BigDecimal averageValuePerHectare,
                              BigDecimal droughtVulnerability, BigDecimal frostVulnerability) {
        Crop c = new Crop();
        c.name = name;
        c.scientificName = scientificName;
        c.averageCycleDays = averageCycleDays;
        c.expectedNdviMin = expectedNdviMin;
        c.expectedNdviMax = expectedNdviMax;
        c.averageValuePerHectare = averageValuePerHectare;
        c.droughtVulnerability = droughtVulnerability;
        c.frostVulnerability = frostVulnerability;
        c.status = true;
        c.createdAt = OffsetDateTime.now();
        return c;
    }

    public UUID getId() { return id; }
    public Integer getCode() { return code; }
    public String getName() { return name; }
    public String getScientificName() { return scientificName; }
    public Integer getAverageCycleDays() { return averageCycleDays; }
    public BigDecimal getExpectedNdviMin() { return expectedNdviMin; }
    public BigDecimal getExpectedNdviMax() { return expectedNdviMax; }
    public BigDecimal getAverageValuePerHectare() { return averageValuePerHectare; }
    public BigDecimal getDroughtVulnerability() { return droughtVulnerability; }
    public BigDecimal getFrostVulnerability() { return frostVulnerability; }
    public Boolean getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getEditedAt() { return editedAt; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }

    public void setName(String name) { this.name = name; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }
    public void setAverageCycleDays(Integer averageCycleDays) { this.averageCycleDays = averageCycleDays; }
    public void setExpectedNdviMin(BigDecimal expectedNdviMin) { this.expectedNdviMin = expectedNdviMin; }
    public void setExpectedNdviMax(BigDecimal expectedNdviMax) { this.expectedNdviMax = expectedNdviMax; }
    public void setAverageValuePerHectare(BigDecimal averageValuePerHectare) { this.averageValuePerHectare = averageValuePerHectare; }
    public void setDroughtVulnerability(BigDecimal droughtVulnerability) { this.droughtVulnerability = droughtVulnerability; }
    public void setFrostVulnerability(BigDecimal frostVulnerability) { this.frostVulnerability = frostVulnerability; }
    public void setStatus(Boolean status) { this.status = status; }
    public void setEditedAt(OffsetDateTime editedAt) { this.editedAt = editedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
}
