package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.CreateFarmRequest;
import com.fiap.zenith.core.application.dto.FarmResponse;
import com.fiap.zenith.core.application.dto.UpdateFarmRequest;
import com.fiap.zenith.core.application.service.FarmService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Endpoints REST do recurso Farm. Respostas usam HATEOAS (EntityModel/CollectionModel).
 * Toda regra de negócio fica no {@link FarmService}; o controller só orquestra HTTP.
 */
@RestController
@RequestMapping("/api/farms")
public class FarmController {

    private final FarmService farmService;

    public FarmController(FarmService farmService) {
        this.farmService = farmService;
    }

    @PostMapping
    public ResponseEntity<EntityModel<FarmResponse>> criar(@Valid @RequestBody CreateFarmRequest req) {
        FarmResponse created = farmService.criar(req);
        EntityModel<FarmResponse> model = toModel(created);
        return ResponseEntity
                .created(linkTo(methodOn(FarmController.class).buscar(created.id())).toUri())
                .body(model);
    }

    @GetMapping("/{id}")
    public EntityModel<FarmResponse> buscar(@PathVariable UUID id) {
        return toModel(farmService.buscarPorId(id));
    }

    @GetMapping
    public CollectionModel<EntityModel<FarmResponse>> listar(@PageableDefault(size = 20) Pageable pageable) {
        Page<FarmResponse> page = farmService.listar(pageable);
        List<EntityModel<FarmResponse>> farms = page.map(this::toModel).getContent();
        return CollectionModel.of(farms,
                linkTo(methodOn(FarmController.class).listar(pageable)).withSelfRel());
    }

    @PutMapping("/{id}")
    public EntityModel<FarmResponse> atualizar(@PathVariable UUID id,
                                               @Valid @RequestBody UpdateFarmRequest req) {
        return toModel(farmService.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        farmService.remover(id);
        return ResponseEntity.noContent().build();
    }

    /** Adiciona os links de navegação (self + coleção) — requisito HATEOAS do edital. */
    private EntityModel<FarmResponse> toModel(FarmResponse farm) {
        return EntityModel.of(farm,
                linkTo(methodOn(FarmController.class).buscar(farm.id())).withSelfRel(),
                linkTo(methodOn(FarmController.class).listar(Pageable.unpaged())).withRel("farms"));
    }
}
