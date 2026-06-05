package com.fiap.zenith.analise_svc.infra.satellite;

import java.math.BigDecimal;

public record SentinelHubObservation(
        BigDecimal ndvi,
        BigDecimal cloudCoveragePct
) {}
