package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.CreateCropRequest;
import com.fiap.zenith.core.application.dto.CropResponse;
import com.fiap.zenith.core.application.dto.UpdateCropRequest;
import com.fiap.zenith.core.application.service.CropService;
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
@RequestMapping("/api/crops")
@Tag(name = "Crops", description = "Catálogo de culturas agrícolas")
public class CropController {

    private final CropService cropService;

    public CropController(CropService cropService) {
        this.cropService = cropService;
    }

    @PostMapping
    @Operation(summary = "Cadastra uma nova cultura")
    public ResponseEntity<EntityModel<CropResponse>> criar(@Valid @RequestBody CreateCropRequest req) {
        CropResponse created = cropService.criar(req);
        return ResponseEntity
                .created(linkTo(methodOn(CropController.class).buscar(created.id())).toUri())
                .body(toModel(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca cultura por ID")
    public EntityModel<CropResponse> buscar(@PathVariable UUID id) {
        return toModel(cropService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista culturas (paginado, resultado cacheado)")
    public CollectionModel<EntityModel<CropResponse>> listar(@PageableDefault(size = 20) Pageable pageable) {
        Page<CropResponse> page = cropService.listar(pageable);
        List<EntityModel<CropResponse>> items = page.map(this::toModel).getContent();
        return CollectionModel.of(items,
                linkTo(methodOn(CropController.class).listar(pageable)).withSelfRel());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza cultura")
    public EntityModel<CropResponse> atualizar(@PathVariable UUID id,
                                               @Valid @RequestBody UpdateCropRequest req) {
        return toModel(cropService.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove cultura (soft delete)")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        cropService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<CropResponse> toModel(CropResponse crop) {
        return EntityModel.of(crop,
                linkTo(methodOn(CropController.class).buscar(crop.id())).withSelfRel(),
                linkTo(methodOn(CropController.class).listar(Pageable.unpaged())).withRel("crops"));
    }
}
