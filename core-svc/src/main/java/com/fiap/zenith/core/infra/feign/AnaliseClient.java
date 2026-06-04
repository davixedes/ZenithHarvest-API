package com.fiap.zenith.core.infra.feign;

import com.fiap.zenith.core.application.dto.NdviHistoricoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

/**
 * Client Feign para o analise-svc — comunicação SÍNCRONA entre serviços
 * (requisito de arquitetura: Feign para leitura, RabbitMQ para eventos).
 * O token JWT da requisição original é propagado pelo interceptor em FeignClientConfig.
 */
@FeignClient(name = "analise-svc", url = "${zenith.clients.analise-uri}")
public interface AnaliseClient {

    @GetMapping("/api/ndvi/{plotId}/historico")
    List<NdviHistoricoResponse> buscarHistoricoNdvi(@PathVariable("plotId") UUID plotId);
}
