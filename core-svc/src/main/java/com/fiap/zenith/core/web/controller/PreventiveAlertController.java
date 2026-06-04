package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.CreatePreventiveAlertRequest;
import com.fiap.zenith.core.application.dto.PreventiveAlertResponse;
import com.fiap.zenith.core.application.dto.UpdatePreventiveAlertSituationRequest;
import com.fiap.zenith.core.application.service.PreventiveAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/preventive-alerts")
@Tag(name = "Preventive Alerts", description = "Alertas preventivos por talhão")
public class PreventiveAlertController {

    private final PreventiveAlertService preventiveAlertService;

    public PreventiveAlertController(PreventiveAlertService preventiveAlertService) {
        this.preventiveAlertService = preventiveAlertService;
    }

    @PostMapping
    @Operation(summary = "Emite alerta preventivo")
    public ResponseEntity<EntityModel<PreventiveAlertResponse>> criar(
            @Valid @RequestBody CreatePreventiveAlertRequest req) {
        PreventiveAlertResponse created = preventiveAlertService.criar(req);
        return ResponseEntity
                .created(linkTo(methodOn(PreventiveAlertController.class).buscar(created.id())).toUri())
                .body(toModel(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca alerta preventivo por ID")
    public EntityModel<PreventiveAlertResponse> buscar(@PathVariable UUID id) {
        return toModel(preventiveAlertService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista alertas preventivos, com filtro opcional por talhão")
    public CollectionModel<EntityModel<PreventiveAlertResponse>> listar(@PageableDefault(size = 20) Pageable pageable,
                                                                        @RequestParam(required = false) UUID plotId) {
        Page<PreventiveAlertResponse> page = preventiveAlertService.listar(plotId, pageable);
        List<EntityModel<PreventiveAlertResponse>> items = page.map(this::toModel).getContent();
        return CollectionModel.of(items,
                linkTo(methodOn(PreventiveAlertController.class).listar(pageable, plotId)).withSelfRel());
    }

    @PostMapping("/{id}/view")
    @Operation(summary = "Marca alerta preventivo como visualizado")
    public EntityModel<PreventiveAlertResponse> visualizar(@PathVariable UUID id) {
        return toModel(preventiveAlertService.marcarComoVisualizado(id));
    }

    @PutMapping("/{id}/situation")
    @Operation(summary = "Atualiza situação do alerta preventivo")
    public EntityModel<PreventiveAlertResponse> atualizarSituacao(@PathVariable UUID id,
                                                                  @Valid @RequestBody UpdatePreventiveAlertSituationRequest req) {
        return toModel(preventiveAlertService.atualizarSituacao(id, req));
    }

    private EntityModel<PreventiveAlertResponse> toModel(PreventiveAlertResponse alert) {
        return EntityModel.of(alert,
                linkTo(methodOn(PreventiveAlertController.class).buscar(alert.id())).withSelfRel(),
                linkTo(methodOn(PreventiveAlertController.class).listar(Pageable.unpaged(), alert.plotId()))
                        .withRel("preventive-alerts"));
    }
}
