package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.CreatePolicyRequest;
import com.fiap.zenith.core.application.dto.PolicyResponse;
import com.fiap.zenith.core.application.service.PolicyService;
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
@RequestMapping("/api/policies")
@Tag(name = "Policies", description = "Apólices de seguro paramétrico")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping
    @Operation(summary = "Emite apólice de seguro")
    public ResponseEntity<EntityModel<PolicyResponse>> criar(@Valid @RequestBody CreatePolicyRequest req) {
        PolicyResponse created = policyService.criar(req);
        return ResponseEntity
                .created(linkTo(methodOn(PolicyController.class).buscar(created.id())).toUri())
                .body(toModel(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca apólice por ID")
    public EntityModel<PolicyResponse> buscar(@PathVariable UUID id) {
        return toModel(policyService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista apólices")
    public CollectionModel<EntityModel<PolicyResponse>> listar(@PageableDefault(size = 20) Pageable pageable) {
        Page<PolicyResponse> page = policyService.listar(pageable);
        List<EntityModel<PolicyResponse>> items = page.map(this::toModel).getContent();
        return CollectionModel.of(items,
                linkTo(methodOn(PolicyController.class).listar(pageable)).withSelfRel());
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancela apólice")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id) {
        policyService.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove apólice (soft delete)")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        policyService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<PolicyResponse> toModel(PolicyResponse p) {
        return EntityModel.of(p,
                linkTo(methodOn(PolicyController.class).buscar(p.id())).withSelfRel(),
                linkTo(methodOn(PolicyController.class).listar(Pageable.unpaged())).withRel("policies"));
    }
}
