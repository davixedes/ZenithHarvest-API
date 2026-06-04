package com.fiap.zenith.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/** Lookup: fontes de imagem satelital (Sentinel-2, Landsat-8, MODIS). */
@Entity
@Table(name = "SatelliteSource")
public class SatelliteSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Description", nullable = false, length = 100)
    private String description;

    @Column(name = "Operator", length = 60)
    private String operator;

    @Column(name = "Resolution", length = 20)
    private String resolution;

    @Column(name = "RevisitFrequencyDays")
    private Integer revisitFrequencyDays;

    @Column(name = "Active", nullable = false)
    private Boolean active = true;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "DeletedAt")
    private OffsetDateTime deletedAt;

    protected SatelliteSource() {}

    public Integer getId() { return id; }
    public String getDescription() { return description; }
    public String getOperator() { return operator; }
    public String getResolution() { return resolution; }
    public Integer getRevisitFrequencyDays() { return revisitFrequencyDays; }
    public Boolean getActive() { return active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
}
