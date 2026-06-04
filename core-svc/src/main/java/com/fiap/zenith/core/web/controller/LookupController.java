package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.service.LookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints read-only para tabelas de domínio (lookups). Respostas são cacheadas em memória
 * (ver {@link com.fiap.zenith.core.config.CacheConfig}) — requisito Cache do edital.
 */
@RestController
@RequestMapping("/api/lookups")
@Tag(name = "Lookups", description = "Tabelas de domínio — read-only, cacheadas")
public class LookupController {

    private final LookupService lookupService;

    public LookupController(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    @GetMapping("/biomes")
    @Operation(summary = "Biomas brasileiros com fator de risco regional")
    public List<LookupService.BiomeItem> biomes() {
        return lookupService.listarBiomes();
    }

    @GetMapping("/production-systems")
    @Operation(summary = "Sistemas de produção agrícola com desconto de prêmio")
    public List<LookupService.ProductionSystemItem> productionSystems() {
        return lookupService.listarProductionSystems();
    }

    @GetMapping("/plot-situations")
    @Operation(summary = "Situações de um talhão no ciclo produtivo")
    public List<LookupService.SituationItem> plotSituations() {
        return lookupService.listarPlotSituations();
    }

    @GetMapping("/insurer-situations")
    @Operation(summary = "Situações de credenciamento de seguradoras")
    public List<LookupService.LookupItem> insurerSituations() {
        return lookupService.listarInsurerSituations();
    }

    @GetMapping("/insurance-situations")
    @Operation(summary = "Situações de disponibilidade de produtos de seguro")
    public List<LookupService.LookupItem> insuranceSituations() {
        return lookupService.listarInsuranceSituations();
    }

    @GetMapping("/quote-situations")
    @Operation(summary = "Situações do ciclo de vida de cotações")
    public List<LookupService.SituationItem> quoteSituations() {
        return lookupService.listarQuoteSituations();
    }

    @GetMapping("/policy-situations")
    @Operation(summary = "Situações do ciclo de vida de apólices")
    public List<LookupService.PolicySituationItem> policySituations() {
        return lookupService.listarPolicySituations();
    }

    @GetMapping("/claim-situations")
    @Operation(summary = "Situações do ciclo de vida de sinistros")
    public List<LookupService.ClaimSituationItem> claimSituations() {
        return lookupService.listarClaimSituations();
    }

    @GetMapping("/claim-event-types")
    @Operation(summary = "Tipos de evento causador de sinistro")
    public List<LookupService.ClaimEventTypeItem> claimEventTypes() {
        return lookupService.listarClaimEventTypes();
    }

    @GetMapping("/claim-categories")
    @Operation(summary = "Categorias de sinistro")
    public List<LookupService.LookupItem> claimCategories() {
        return lookupService.listarClaimCategories();
    }

    @GetMapping("/claim-subcategories")
    @Operation(summary = "Subcategorias de sinistro")
    public List<LookupService.LookupItem> claimSubCategories() {
        return lookupService.listarClaimSubCategories();
    }

    @GetMapping("/rejection-reasons")
    @Operation(summary = "Motivos de rejeição de sinistro")
    public List<LookupService.LookupItem> rejectionReasons() {
        return lookupService.listarRejectionReasons();
    }

    @GetMapping("/satellite-sources")
    @Operation(summary = "Fontes de imagem satelital (Sentinel-2, Landsat-8, MODIS)")
    public List<LookupService.SatelliteSourceItem> satelliteSources() {
        return lookupService.listarSatelliteSources();
    }

    @GetMapping("/satellite-classes")
    @Operation(summary = "Classes de análise satelital por nível de estresse de vegetação")
    public List<LookupService.SatelliteClassItem> satelliteClasses() {
        return lookupService.listarSatelliteClasses();
    }

    @GetMapping("/alert-types")
    @Operation(summary = "Tipos de alerta preventivo")
    public List<LookupService.LookupItem> alertTypes() {
        return lookupService.listarAlertTypes();
    }

    @GetMapping("/alert-severities")
    @Operation(summary = "Severidades de alerta com cor visual e flags de notificação")
    public List<LookupService.AlertSeverityItem> alertSeverities() {
        return lookupService.listarAlertSeverities();
    }

    @GetMapping("/alert-situations")
    @Operation(summary = "Situações de ciclo de vida de alertas preventivos")
    public List<LookupService.SituationItem> alertSituations() {
        return lookupService.listarAlertSituations();
    }

    @GetMapping("/payment-types")
    @Operation(summary = "Tipos de pagamento com direção (IN/OUT)")
    public List<LookupService.PaymentTypeItem> paymentTypes() {
        return lookupService.listarPaymentTypes();
    }

    @GetMapping("/payment-situations")
    @Operation(summary = "Situações de pagamento")
    public List<LookupService.PaymentSituationItem> paymentSituations() {
        return lookupService.listarPaymentSituations();
    }
}
