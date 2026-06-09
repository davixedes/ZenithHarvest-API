package com.fiap.zenith.analise_svc.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Read model somente-leitura da cultura agrícola — acesso JPA direto ao Postgres compartilhado. */
@Entity
@Immutable
@Table(name = "Crop")
public class CropInfo {

    @Id
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "Name", length = 80)
    private String name;

    @Column(name = "ExpectedNdviMin", precision = 4, scale = 3)
    private BigDecimal expectedNdviMin;

    @Column(name = "ExpectedNdviMax", precision = 4, scale = 3)
    private BigDecimal expectedNdviMax;

    @Column(name = "DeletedAt")
    private OffsetDateTime deletedAt;

    protected CropInfo() {}

    public UUID getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getExpectedNdviMin() { return expectedNdviMin; }
    public BigDecimal getExpectedNdviMax() { return expectedNdviMax; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
}
