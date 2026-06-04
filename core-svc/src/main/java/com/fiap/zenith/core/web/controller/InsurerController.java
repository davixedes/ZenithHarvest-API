package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.CreateInsurerRequest;
import com.fiap.zenith.core.application.dto.InsurerResponse;
import com.fiap.zenith.core.application.service.InsurerService;
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
@RequestMapping("/api/insurers")
@Tag(name = "Insurers", description = "Seguradoras credenciadas")
public class InsurerController {

    private final InsurerService insurerService;

    public InsurerController(InsurerService insurerService) {
        this.insurerService = insurerService;
    }

    @PostMapping
    @Operation(summary = "Credencia uma seguradora")
    public ResponseEntity<EntityModel<InsurerResponse>> criar(@Valid @RequestBody CreateInsurerRequest req) {
        InsurerResponse created = insurerService.criar(req);
        return ResponseEntity
                .created(linkTo(methodOn(InsurerController.class).buscar(created.id())).toUri())
                .body(toModel(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca seguradora por ID")
    public EntityModel<InsurerResponse> buscar(@PathVariable UUID id) {
        return toModel(insurerService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista seguradoras")
    public CollectionModel<EntityModel<InsurerResponse>> listar(@PageableDefault(size = 20) Pageable pageable) {
        Page<InsurerResponse> page = insurerService.listar(pageable);
        List<EntityModel<InsurerResponse>> items = page.map(this::toModel).getContent();
        return CollectionModel.of(items,
                linkTo(methodOn(InsurerController.class).listar(pageable)).withSelfRel());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza seguradora")
    public EntityModel<InsurerResponse> atualizar(@PathVariable UUID id,
                                                   @Valid @RequestBody CreateInsurerRequest req) {
        return toModel(insurerService.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove seguradora (soft delete)")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        insurerService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<InsurerResponse> toModel(InsurerResponse ins) {
        return EntityModel.of(ins,
                linkTo(methodOn(InsurerController.class).buscar(ins.id())).withSelfRel(),
                linkTo(methodOn(InsurerController.class).listar(Pageable.unpaged())).withRel("insurers"));
    }
}
