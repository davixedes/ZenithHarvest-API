package com.fiap.zenith.analise_svc.infra.satellite;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zenith.satellite.sentinel-hub")
public class SentinelHubProperties {

    private boolean enabled;
    private String clientId;
    private String clientSecret;
    private String tokenUrl = "https://services.sentinel-hub.com/auth/realms/main/protocol/openid-connect/token";
    private String statisticsUrl = "https://services.sentinel-hub.com/api/v1/statistics";
    private String collection = "sentinel-2-l2a";
    private int daysBack = 10;
    private int resolutionMeters = 10;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getStatisticsUrl() {
        return statisticsUrl;
    }

    public void setStatisticsUrl(String statisticsUrl) {
        this.statisticsUrl = statisticsUrl;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public int getDaysBack() {
        return daysBack;
    }

    public void setDaysBack(int daysBack) {
        this.daysBack = daysBack;
    }

    public int getResolutionMeters() {
        return resolutionMeters;
    }

    public void setResolutionMeters(int resolutionMeters) {
        this.resolutionMeters = resolutionMeters;
    }
}
