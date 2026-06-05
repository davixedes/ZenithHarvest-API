package com.fiap.zenith.analise_svc.infra.satellite;

import com.fiap.zenith.analise_svc.infra.messaging.SinistroAbertoEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SentinelHubStatsRequestFactory {

    private static final double METERS_PER_DEGREE_LAT = 111_320d;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final String EVALSCRIPT = """
            //VERSION=3
            function setup() {
              return {
                input: [{
                  bands: ["B04", "B08", "dataMask"]
                }],
                output: [
                  {
                    id: "output_NDVI",
                    bands: 1,
                    sampleType: "FLOAT32"
                  },
                  {
                    id: "dataMask",
                    bands: 1
                  }
                ]
              };
            }

            function evaluatePixel(samples) {
              let denominator = samples.B08 + samples.B04;
              let ndvi = denominator === 0 ? 0 : (samples.B08 - samples.B04) / denominator;
              return {
                output_NDVI: [ndvi],
                dataMask: [samples.dataMask]
              };
            }
            """;

    public Map<String, Object> build(SinistroAbertoEvent evento, SentinelHubProperties properties) {
        BoundingBox bbox = buildBoundingBox(evento.openingGpsLat(), evento.openingGpsLng(), evento.plotAreaM2());
        OffsetDateTime to = OffsetDateTime.now();
        OffsetDateTime from = to.minusDays(properties.getDaysBack());

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("input", buildInput(properties, bbox));
        root.put("aggregation", buildAggregation(properties, from, to));
        root.put("calculations", buildCalculations());
        return root;
    }

    BoundingBox buildBoundingBox(BigDecimal latitude, BigDecimal longitude, BigDecimal areaM2) {
        if (latitude == null || longitude == null || areaM2 == null || areaM2.signum() <= 0) {
            throw new IllegalArgumentException("Evento sem coordenadas/área suficientes para consulta satelital.");
        }

        double lat = latitude.doubleValue();
        double lon = longitude.doubleValue();
        double halfSideMeters = Math.sqrt(areaM2.doubleValue()) / 2d;
        double latDelta = halfSideMeters / METERS_PER_DEGREE_LAT;
        double lonDenominator = METERS_PER_DEGREE_LAT * Math.cos(Math.toRadians(lat));
        double lonDelta = lonDenominator == 0d ? latDelta : halfSideMeters / lonDenominator;

        return new BoundingBox(
                round(lon - lonDelta),
                round(lat - latDelta),
                round(lon + lonDelta),
                round(lat + latDelta)
        );
    }

    private Map<String, Object> buildInput(SentinelHubProperties properties, BoundingBox bbox) {
        Map<String, Object> bounds = new LinkedHashMap<>();
        bounds.put("bbox", List.of(bbox.minLon(), bbox.minLat(), bbox.maxLon(), bbox.maxLat()));
        bounds.put("properties", Map.of("crs", "http://www.opengis.net/def/crs/OGC/1.3/CRS84"));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", properties.getCollection());
        data.put("dataFilter", Map.of("mosaickingOrder", "leastCC"));

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("bounds", bounds);
        input.put("data", List.of(data));
        return input;
    }

    private Map<String, Object> buildAggregation(SentinelHubProperties properties, OffsetDateTime from, OffsetDateTime to) {
        Map<String, Object> timeRange = new LinkedHashMap<>();
        timeRange.put("from", ISO_FORMATTER.format(from));
        timeRange.put("to", ISO_FORMATTER.format(to));

        Map<String, Object> aggregation = new LinkedHashMap<>();
        aggregation.put("timeRange", timeRange);
        aggregation.put("aggregationInterval", Map.of("of", "P1D"));
        double resolutionDeg = properties.getResolutionMeters() / METERS_PER_DEGREE_LAT;
        aggregation.put("evalscript", EVALSCRIPT);
        aggregation.put("resx", resolutionDeg);
        aggregation.put("resy", resolutionDeg);
        return aggregation;
    }

    private Map<String, Object> buildCalculations() {
        return Map.of(
                "default",
                Map.of(
                        "statistics",
                        Map.of(
                                "default",
                                Map.of()
                        )
                )
        );
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP).doubleValue();
    }

    record BoundingBox(double minLon, double minLat, double maxLon, double maxLat) {
    }
}
