package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.CreatePlotRequest;
import com.fiap.zenith.core.application.dto.PlotResponse;
import com.fiap.zenith.core.application.dto.UpdatePlotRequest;
import com.fiap.zenith.core.application.service.PlotService;
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
@Tag(name = "Plots", description = "Talhões de fazendas")
public class PlotController {

    private final PlotService plotService;

    public PlotController(PlotService plotService) {
        this.plotService = plotService;
    }

    @PostMapping("/api/plots")
    @Operation(summary = "Cria um talhão")
    public ResponseEntity<EntityModel<PlotResponse>> criar(@Valid @RequestBody CreatePlotRequest req) {
        PlotResponse created = plotService.criar(req);
        return ResponseEntity
                .created(linkTo(methodOn(PlotController.class).buscar(created.id())).toUri())
                .body(toModel(created));
    }

    @GetMapping("/api/plots/{id}")
    @Operation(summary = "Busca talhão por ID")
    public EntityModel<PlotResponse> buscar(@PathVariable UUID id) {
        return toModel(plotService.buscarPorId(id));
    }

    @GetMapping("/api/plots")
    @Operation(summary = "Lista todos os talhões")
    public CollectionModel<EntityModel<PlotResponse>> listar(@PageableDefault(size = 20) Pageable pageable) {
        Page<PlotResponse> page = plotService.listar(pageable);
        List<EntityModel<PlotResponse>> items = page.map(this::toModel).getContent();
        return CollectionModel.of(items,
                linkTo(methodOn(PlotController.class).listar(pageable)).withSelfRel());
    }

    @GetMapping("/api/farms/{farmId}/plots")
    @Operation(summary = "Lista talhões de uma fazenda")
    public CollectionModel<EntityModel<PlotResponse>> listarPorFazenda(
            @PathVariable UUID farmId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PlotResponse> page = plotService.listarPorFazenda(farmId, pageable);
        List<EntityModel<PlotResponse>> items = page.map(this::toModel).getContent();
        return CollectionModel.of(items,
                linkTo(methodOn(PlotController.class).listarPorFazenda(farmId, pageable)).withSelfRel());
    }

    @PutMapping("/api/plots/{id}")
    @Operation(summary = "Atualiza talhão")
    public EntityModel<PlotResponse> atualizar(@PathVariable UUID id,
                                               @Valid @RequestBody UpdatePlotRequest req) {
        return toModel(plotService.atualizar(id, req));
    }

    @DeleteMapping("/api/plots/{id}")
    @Operation(summary = "Remove talhão (soft delete)")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        plotService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<PlotResponse> toModel(PlotResponse plot) {
        return EntityModel.of(plot,
                linkTo(methodOn(PlotController.class).buscar(plot.id())).withSelfRel(),
                linkTo(methodOn(PlotController.class).listar(Pageable.unpaged())).withRel("plots"));
    }
}
