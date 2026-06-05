package com.fiap.zenith.analise_svc.infra.satellite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.zenith.analise_svc.infra.messaging.SinistroAbertoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class SentinelHubService {

    private static final Logger log = LoggerFactory.getLogger(SentinelHubService.class);
    private static final BigDecimal DEFAULT_CLOUD_COVERAGE = new BigDecimal("5.00");

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final SentinelHubProperties properties;
    private final SentinelHubStatsRequestFactory requestFactory;

    private String accessToken;
    private Instant accessTokenExpiresAt = Instant.EPOCH;

    public SentinelHubService(RestClient.Builder restClientBuilder,
                              ObjectMapper objectMapper,
                              SentinelHubProperties properties,
                              SentinelHubStatsRequestFactory requestFactory) {
        SimpleClientHttpRequestFactory httpFactory = new SimpleClientHttpRequestFactory();
        httpFactory.setConnectTimeout(Duration.ofSeconds(5));
        httpFactory.setReadTimeout(Duration.ofSeconds(15));
        this.restClient = restClientBuilder.requestFactory(httpFactory).build();
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.requestFactory = requestFactory;
    }

    public Optional<SentinelHubObservation> buscarNdvi(SinistroAbertoEvent evento) {
        if (!properties.isEnabled() || isBlank(properties.getClientId()) || isBlank(properties.getClientSecret())) {
            return Optional.empty();
        }
        if (evento.openingGpsLat() == null || evento.openingGpsLng() == null || evento.plotAreaM2() == null) {
            return Optional.empty();
        }

        try {
            JsonNode response = restClient.post()
                    .uri(properties.getStatisticsUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(getAccessToken()))
                    .body(requestFactory.build(evento, properties))
                    .retrieve()
                    .body(JsonNode.class);

            BigDecimal ndvi = extractMeanNdvi(response);
            return Optional.of(new SentinelHubObservation(ndvi, DEFAULT_CLOUD_COVERAGE));
        } catch (RestClientException | IllegalArgumentException ex) {
            throw new SentinelHubIntegrationException("Falha ao consultar Sentinel Hub.", ex);
        }
    }

    private synchronized String getAccessToken() {
        if (accessToken != null && Instant.now().isBefore(accessTokenExpiresAt.minusSeconds(30))) {
            return accessToken;
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", properties.getClientId());
        formData.add("client_secret", properties.getClientSecret());

        JsonNode tokenResponse = restClient.post()
                .uri(properties.getTokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(formData)
                .retrieve()
                .body(JsonNode.class);

        if (tokenResponse == null || tokenResponse.path("access_token").isMissingNode()) {
            throw new SentinelHubIntegrationException("Resposta inválida ao obter token do Sentinel Hub.");
        }

        accessToken = tokenResponse.path("access_token").asText();
        int expiresIn = tokenResponse.path("expires_in").asInt(3600);
        accessTokenExpiresAt = Instant.now().plusSeconds(expiresIn);
        return accessToken;
    }

    private BigDecimal extractMeanNdvi(JsonNode response) {
        if (response == null) {
            throw new SentinelHubIntegrationException("Resposta vazia do Sentinel Hub.");
        }

        JsonNode data = response.path("data");
        if (!data.isArray() || data.isEmpty()) {
            throw new SentinelHubIntegrationException("Sentinel Hub não retornou observações para o período.");
        }

        JsonNode latest = data.get(data.size() - 1);
        JsonNode meanNode = latest.path("outputs").path("output_NDVI").path("bands").path("B0").path("stats").path("mean");
        if (meanNode.isMissingNode() || !meanNode.isNumber()) {
            log.warn("Resposta Sentinel Hub sem mean de NDVI: {}", safeJson(response));
            throw new SentinelHubIntegrationException("Sentinel Hub não retornou mean de NDVI.");
        }

        return BigDecimal.valueOf(meanNode.asDouble()).setScale(3, RoundingMode.HALF_UP);
    }

    private String safeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            return "<json-indisponivel>";
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
