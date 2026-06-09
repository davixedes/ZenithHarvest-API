package com.fiap.zenith.analise_svc.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Read model somente-leitura do talhão — acesso JPA direto ao Postgres compartilhado. */
@Entity
@Immutable
@Table(name = "Plot")
public class PlotInfo {

    @Id
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "FarmId")
    private UUID farmId;

    @Column(name = "CropId")
    private UUID cropId;

    @Column(name = "DeletedAt")
    private OffsetDateTime deletedAt;

    protected PlotInfo() {}

    public UUID getId() { return id; }
    public UUID getFarmId() { return farmId; }
    public UUID getCropId() { return cropId; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
}
