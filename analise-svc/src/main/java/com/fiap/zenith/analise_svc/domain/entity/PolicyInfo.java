package com.fiap.zenith.analise_svc.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Read model somente-leitura da apólice — acesso JPA direto ao Postgres compartilhado. */
@Entity
@Immutable
@Table(name = "Policy")
public class PolicyInfo {

    @Id
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "PlotId", nullable = false)
    private UUID plotId;

    @Column(name = "PolicySituationId", nullable = false)
    private Integer policySituationId;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate;

    @Column(name = "DeletedAt")
    private OffsetDateTime deletedAt;

    protected PolicyInfo() {}

    public UUID getId() { return id; }
    public UUID getPlotId() { return plotId; }
    public Integer getPolicySituationId() { return policySituationId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
}
