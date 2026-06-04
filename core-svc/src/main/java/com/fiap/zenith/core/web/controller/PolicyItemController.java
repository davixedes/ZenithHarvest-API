package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.CreatePolicyItemRequest;
import com.fiap.zenith.core.application.dto.PolicyItemResponse;
import com.fiap.zenith.core.application.dto.UpdatePolicyItemRequest;
import com.fiap.zenith.core.application.service.PolicyItemService;
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
@RequestMapping("/api/policy-items")
@Tag(name = "Policy Items", description = "Itens de cobertura da apólice")
public class PolicyItemController {

    private final PolicyItemService policyItemService;

    public PolicyItemController(PolicyItemService policyItemService) {
        this.policyItemService = policyItemService;
    }

    @PostMapping
    @Operation(summary = "Cria item de cobertura para uma apólice")
    public ResponseEntity<EntityModel<PolicyItemResponse>> criar(@Valid @RequestBody CreatePolicyItemRequest req) {
        PolicyItemResponse created = policyItemService.criar(req);
        return ResponseEntity
                .created(linkTo(methodOn(PolicyItemController.class).buscar(created.id())).toUri())
                .body(toModel(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca item de apólice por ID")
    public EntityModel<PolicyItemResponse> buscar(@PathVariable UUID id) {
        return toModel(policyItemService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista itens de apólice, com filtro opcional por apólice")
    public CollectionModel<EntityModel<PolicyItemResponse>> listar(@PageableDefault(size = 20) Pageable pageable,
                                                                   @RequestParam(required = false) UUID policyId) {
        Page<PolicyItemResponse> page = policyItemService.listar(policyId, pageable);
        List<EntityModel<PolicyItemResponse>> items = page.map(this::toModel).getContent();
        return CollectionModel.of(items,
                linkTo(methodOn(PolicyItemController.class).listar(pageable, policyId)).withSelfRel());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza percentuais e observações do item de apólice")
    public EntityModel<PolicyItemResponse> atualizar(@PathVariable UUID id,
                                                     @Valid @RequestBody UpdatePolicyItemRequest req) {
        return toModel(policyItemService.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove item de apólice")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        policyItemService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<PolicyItemResponse> toModel(PolicyItemResponse item) {
        return EntityModel.of(item,
                linkTo(methodOn(PolicyItemController.class).buscar(item.id())).withSelfRel(),
                linkTo(methodOn(PolicyItemController.class).listar(Pageable.unpaged(), item.policyId()))
                        .withRel("policy-items"));
    }
}
