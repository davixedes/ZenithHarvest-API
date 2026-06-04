package com.fiap.zenith.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Alerta preventivo de queda de NDVI/risco emitido para um talhão —
 * diferencial do produto: avisa o produtor ANTES da perda total.
 */
@Entity
@Table(name = "PreventiveAlert")
public class PreventiveAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "PlotId", nullable = false)
    private UUID plotId;

    @Column(name = "AlertTypeId", nullable = false)
    private Integer alertTypeId;

    @Column(name = "AlertSeverityId", nullable = false)
    private Integer alertSeverityId;

    @Column(name = "AlertSituationId", nullable = false)
    private Integer alertSituationId;

    @Column(name = "Message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "ObservedNdvi", precision = 4, scale = 3)
    private BigDecimal observedNdvi;

    @Column(name = "ExpectedNdvi", precision = 4, scale = 3)
    private BigDecimal expectedNdvi;

    @Column(name = "DropPct", precision = 5, scale = 2)
    private BigDecimal dropPct;

    @Column(name = "IssuedAt", nullable = false, updatable = false)
    private OffsetDateTime issuedAt;

    @Column(name = "ViewedAt")
    private OffsetDateTime viewedAt;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected PreventiveAlert() {}

    public static PreventiveAlert create(UUID plotId, Integer alertTypeId,
                                          Integer alertSeverityId, Integer alertSituationId,
                                          String message, BigDecimal observedNdvi,
                                          BigDecimal expectedNdvi, BigDecimal dropPct) {
        PreventiveAlert alert = new PreventiveAlert();
        alert.plotId = plotId;
        alert.alertTypeId = alertTypeId;
        alert.alertSeverityId = alertSeverityId;
        alert.alertSituationId = alertSituationId;
        alert.message = message;
        alert.observedNdvi = observedNdvi;
        alert.expectedNdvi = expectedNdvi;
        alert.dropPct = dropPct;
        alert.issuedAt = OffsetDateTime.now();
        alert.createdAt = OffsetDateTime.now();
        return alert;
    }

    public UUID getId() { return id; }
    public UUID getPlotId() { return plotId; }
    public Integer getAlertTypeId() { return alertTypeId; }
    public Integer getAlertSeverityId() { return alertSeverityId; }
    public Integer getAlertSituationId() { return alertSituationId; }
    public String getMessage() { return message; }
    public BigDecimal getObservedNdvi() { return observedNdvi; }
    public BigDecimal getExpectedNdvi() { return expectedNdvi; }
    public BigDecimal getDropPct() { return dropPct; }
    public OffsetDateTime getIssuedAt() { return issuedAt; }
    public OffsetDateTime getViewedAt() { return viewedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setAlertSituationId(Integer alertSituationId) { this.alertSituationId = alertSituationId; }
    public void setViewedAt(OffsetDateTime viewedAt) { this.viewedAt = viewedAt; }
}
