package com.fiap.zenith.analise_svc.domain.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Série temporal de NDVI por talhão — armazenada no MongoDB.
 * Cada documento representa uma leitura satelital de um dia.
 */
@Document(collection = "historico_ndvi")
public class HistoricoNdviDocument {

    @Id
    private String id;

    @Indexed
    private UUID plotId;

    @Indexed
    private UUID claimId;

    private LocalDate imageDate;
    private BigDecimal meanNdvi;
    private BigDecimal meanEvi;
    private String satelliteSource;
    private BigDecimal cloudCoveragePct;
    private OffsetDateTime createdAt;

    protected HistoricoNdviDocument() {}

    public static HistoricoNdviDocument create(UUID plotId, UUID claimId, LocalDate imageDate,
                                                BigDecimal meanNdvi, BigDecimal meanEvi,
                                                String satelliteSource, BigDecimal cloudCoveragePct) {
        HistoricoNdviDocument doc = new HistoricoNdviDocument();
        doc.plotId = plotId;
        doc.claimId = claimId;
        doc.imageDate = imageDate;
        doc.meanNdvi = meanNdvi;
        doc.meanEvi = meanEvi;
        doc.satelliteSource = satelliteSource;
        doc.cloudCoveragePct = cloudCoveragePct;
        doc.createdAt = OffsetDateTime.now();
        return doc;
    }

    public String getId() { return id; }
    public UUID getPlotId() { return plotId; }
    public UUID getClaimId() { return claimId; }
    public LocalDate getImageDate() { return imageDate; }
    public BigDecimal getMeanNdvi() { return meanNdvi; }
    public BigDecimal getMeanEvi() { return meanEvi; }
    public String getSatelliteSource() { return satelliteSource; }
    public BigDecimal getCloudCoveragePct() { return cloudCoveragePct; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
