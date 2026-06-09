package com.fiap.zenith.analise_svc.web.controller;

import com.fiap.zenith.analise_svc.application.service.AlertaDeteccaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gatilho manual da varredura de alertas — para demo e testes.
 * Na apresentação: POST /api/varredura/alertas dispara a detecção imediatamente.
 */
@RestController
@RequestMapping("/api/varredura")
public class VarreduraController {

    private final AlertaDeteccaoService deteccaoService;

    public VarreduraController(AlertaDeteccaoService deteccaoService) {
        this.deteccaoService = deteccaoService;
    }

    @PostMapping("/alertas")
    public ResponseEntity<String> dispararVarredura() {
        deteccaoService.executarVarredura();
        return ResponseEntity.ok("Varredura de alertas concluída.");
    }
}
