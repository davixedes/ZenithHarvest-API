package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.CreatePreventiveAlertRequest;
import com.fiap.zenith.core.application.dto.PreventiveAlertResponse;
import com.fiap.zenith.core.application.service.AlertaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    public CollectionModel<EntityModel<PreventiveAlertResponse>> listar(
            @RequestParam(required = false) UUID plotId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<PreventiveAlertResponse> page = plotId != null
                ? alertaService.listarPorPlot(plotId, pageable)
                : alertaService.listar(pageable);

        List<EntityModel<PreventiveAlertResponse>> items = page.map(this::toModel).getContent();
        return CollectionModel.of(items,
                linkTo(methodOn(AlertaController.class).listar(plotId, pageable)).withSelfRel());
    }

    @GetMapping("/{id}")
    public EntityModel<PreventiveAlertResponse> buscar(@PathVariable UUID id) {
        return toModel(alertaService.buscarPorId(id));
    }

    @PutMapping("/{id}/visualizar")
    public EntityModel<PreventiveAlertResponse> visualizar(@PathVariable UUID id) {
        return toModel(alertaService.visualizar(id));
    }

    @PutMapping("/{id}/resolver")
    public EntityModel<PreventiveAlertResponse> resolver(@PathVariable UUID id) {
        return toModel(alertaService.resolver(id));
    }

    @PutMapping("/{id}/descartar")
    public EntityModel<PreventiveAlertResponse> descartar(@PathVariable UUID id) {
        return toModel(alertaService.descartar(id));
    }

    /**
     * Rota interna — chamada exclusivamente pelo analise-svc via Feign.
     * Não é roteada pelo gateway; liberada sem autenticação no SecurityConfig.
     */
    @PostMapping("/interno")
    public ResponseEntity<PreventiveAlertResponse> criarInterno(
            @Valid @RequestBody CreatePreventiveAlertRequest req) {
        return ResponseEntity.ok(alertaService.criar(req));
    }

    private EntityModel<PreventiveAlertResponse> toModel(PreventiveAlertResponse r) {
        EntityModel<PreventiveAlertResponse> model = EntityModel.of(r,
                linkTo(methodOn(AlertaController.class).buscar(r.id())).withSelfRel(),
                linkTo(methodOn(AlertaController.class).listar(r.plotId(), Pageable.unpaged())).withRel("alertas"));

        // Links de transição: só adiciona se o estado não for terminal (3=Resolvido, 4=Descartado)
        if (r.alertSituationId() < 3) {
            if (r.alertSituationId() == 1) {
                model.add(linkTo(methodOn(AlertaController.class).visualizar(r.id())).withRel("visualizar"));
            }
            model.add(linkTo(methodOn(AlertaController.class).resolver(r.id())).withRel("resolver"));
            model.add(linkTo(methodOn(AlertaController.class).descartar(r.id())).withRel("descartar"));
        }

        return model;
    }
}
