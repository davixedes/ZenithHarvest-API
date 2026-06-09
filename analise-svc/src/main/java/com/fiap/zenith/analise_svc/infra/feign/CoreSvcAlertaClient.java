package com.fiap.zenith.analise_svc.infra.feign;

import com.fiap.zenith.analise_svc.infra.feign.dto.AlertaResponse;
import com.fiap.zenith.analise_svc.infra.feign.dto.CriarAlertaRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "core-svc-alertas", url = "${zenith.core-svc.url}")
public interface CoreSvcAlertaClient {

    @PostMapping("/api/alertas/interno")
    AlertaResponse criarAlerta(@RequestBody CriarAlertaRequest request);
}
