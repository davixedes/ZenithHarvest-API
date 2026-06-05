package com.fiap.zenith.analise_svc.infra.satellite;

import com.fiap.zenith.analise_svc.infra.messaging.SinistroAbertoEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class SentinelHubStatsRequestFactoryTest {

    private final SentinelHubStatsRequestFactory factory = new SentinelHubStatsRequestFactory();

    @Test
    @SuppressWarnings("unchecked")
    void buildDeveMontarPayloadEsperado() {
        SentinelHubProperties properties = new SentinelHubProperties();
        properties.setCollection("sentinel-2-l2a");
        properties.setDaysBack(7);
        properties.setResolutionMeters(10);

        Map<String, Object> payload = factory.build(evento(), properties);

        Map<String, Object> input = (Map<String, Object>) payload.get("input");
        Map<String, Object> bounds = (Map<String, Object>) input.get("bounds");
        List<Double> bbox = (List<Double>) bounds.get("bbox");
        Map<String, Object> aggregation = (Map<String, Object>) payload.get("aggregation");

        assertThat(bbox).hasSize(4);
        assertThat((String) aggregation.get("evalscript")).contains("output_NDVI");
        double expectedResDeg = 10.0 / 111_320.0;
        assertThat((Double) aggregation.get("resx")).isCloseTo(expectedResDeg, within(1e-9));
        assertThat((Double) aggregation.get("resy")).isCloseTo(expectedResDeg, within(1e-9));
    }

    @Test
    void buildBoundingBoxDeveFalharSemDadosMinimos() {
        assertThatThrownBy(() -> factory.buildBoundingBox(null, new BigDecimal("-47.0"), new BigDecimal("10000")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private SinistroAbertoEvent evento() {
        return new SinistroAbertoEvent(
                UUID.randomUUID(),
                "CLM-2026-0001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                1,
                2,
                new BigDecimal("0.600"),
                new BigDecimal("-22.9100"),
                new BigDecimal("-47.0650"),
                "Queda de vigor",
                new BigDecimal("120000.00"),
                new BigDecimal("40000.00")
        );
    }
}
