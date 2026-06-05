package com.fiap.zenith.analise_svc.infra.satellite;

public class SentinelHubIntegrationException extends RuntimeException {

    public SentinelHubIntegrationException(String message) {
        super(message);
    }

    public SentinelHubIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
