package com.fiap.zenith.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

/**
 * Rotas do gateway (Spring Cloud Gateway Server WebMVC), definidas programaticamente.
 *
 * <p>Encaminha sem reescrever o path: {@code /auth/**} e {@code /api/**} vão para o core-svc
 * (8081); {@code /analise/**} fica reservado para o analise-svc (8082) em fases futuras.
 * Os destinos são parametrizáveis por env (CORE_SVC_URI / ANALISE_SVC_URI).</p>
 */
@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouterFunction<ServerResponse> coreRoutes(
            org.springframework.core.env.Environment env) {
        String coreUri = env.getProperty("zenith.gateway.core-uri", "http://localhost:8081");
        return route("core-svc")
                .route(path("/auth/**"), http(URI.create(coreUri)))
                .route(path("/api/**"), http(URI.create(coreUri)))
                .route(path("/swagger-ui/**"), http(URI.create(coreUri)))
                .route(path("/v3/api-docs/**"), http(URI.create(coreUri)))
                .build();
    }
}
