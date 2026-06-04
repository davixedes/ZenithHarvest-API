package com.fiap.zenith.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/** Lookup: classes de análise satelital por nível de estresse de vegetação (0=saudável→4=solo exposto). */
@Entity
@Table(name = "SatelliteClass")
public class SatelliteClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Description", nullable = false, length = 100)
    private String description;

    @Column(name = "Severity", nullable = false)
    private Integer severity = 0;

    @Column(name = "Active", nullable = false)
    private Boolean active = true;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected SatelliteClass() {}

    public Integer getId() { return id; }
    public String getDescription() { return description; }
    public Integer getSeverity() { return severity; }
    public Boolean getActive() { return active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
