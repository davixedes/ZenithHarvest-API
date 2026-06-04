package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.ClaimItemResponse;
import com.fiap.zenith.core.application.dto.CreateClaimItemRequest;
import com.fiap.zenith.core.application.dto.UpdateClaimItemRequest;
import com.fiap.zenith.core.application.service.ClaimItemService;
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
@RequestMapping("/api/claim-items")
@Tag(name = "Claim Items", description = "Itens de composição de um sinistro")
public class ClaimItemController {

    private final ClaimItemService claimItemService;

    public ClaimItemController(ClaimItemService claimItemService) {
        this.claimItemService = claimItemService;
    }

    @PostMapping
    @Operation(summary = "Cria item de composição de um sinistro")
    public ResponseEntity<EntityModel<ClaimItemResponse>> criar(@Valid @RequestBody CreateClaimItemRequest req) {
        ClaimItemResponse created = claimItemService.criar(req);
        return ResponseEntity
                .created(linkTo(methodOn(ClaimItemController.class).buscar(created.id())).toUri())
                .body(toModel(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca item de sinistro por ID")
    public EntityModel<ClaimItemResponse> buscar(@PathVariable UUID id) {
        return toModel(claimItemService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista itens de sinistro, com filtro opcional por sinistro")
    public CollectionModel<EntityModel<ClaimItemResponse>> listar(@PageableDefault(size = 20) Pageable pageable,
                                                                  @RequestParam(required = false) UUID claimId) {
        Page<ClaimItemResponse> page = claimItemService.listar(claimId, pageable);
        List<EntityModel<ClaimItemResponse>> items = page.map(this::toModel).getContent();
        return CollectionModel.of(items,
                linkTo(methodOn(ClaimItemController.class).listar(pageable, claimId)).withSelfRel());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza métricas do item de sinistro")
    public EntityModel<ClaimItemResponse> atualizar(@PathVariable UUID id,
                                                    @Valid @RequestBody UpdateClaimItemRequest req) {
        return toModel(claimItemService.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove item de sinistro")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        claimItemService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<ClaimItemResponse> toModel(ClaimItemResponse item) {
        return EntityModel.of(item,
                linkTo(methodOn(ClaimItemController.class).buscar(item.id())).withSelfRel(),
                linkTo(methodOn(ClaimItemController.class).listar(Pageable.unpaged(), item.claimId()))
                        .withRel("claim-items"));
    }
}
