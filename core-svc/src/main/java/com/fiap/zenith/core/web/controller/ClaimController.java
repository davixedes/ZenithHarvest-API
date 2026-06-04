package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.ClaimResponse;
import com.fiap.zenith.core.application.dto.CreateClaimRequest;
import com.fiap.zenith.core.application.service.ClaimService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/claims")
@Tag(name = "Claims", description = "Sinistros — criação dispara análise no analise-svc via RabbitMQ")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    @Operation(summary = "Abre sinistro e dispara evento RabbitMQ sinistro.aberto")
    public ResponseEntity<EntityModel<ClaimResponse>> abrir(@Valid @RequestBody CreateClaimRequest req) {
        ClaimResponse created = claimService.abrir(req);
        return ResponseEntity
                .created(linkTo(methodOn(ClaimController.class).buscar(created.id())).toUri())
                .body(toModel(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca sinistro por ID")
    public EntityModel<ClaimResponse> buscar(@PathVariable UUID id) {
        return toModel(claimService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista sinistros")
    public CollectionModel<EntityModel<ClaimResponse>> listar(@PageableDefault(size = 20) Pageable pageable) {
        Page<ClaimResponse> page = claimService.listar(pageable);
        List<EntityModel<ClaimResponse>> items = page.map(this::toModel).getContent();
        return CollectionModel.of(items,
                linkTo(methodOn(ClaimController.class).listar(pageable)).withSelfRel());
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Aprova sinistro com valor de indenização")
    public EntityModel<ClaimResponse> aprovar(@PathVariable UUID id,
                                               @RequestParam BigDecimal approvedAmount) {
        return toModel(claimService.aprovar(id, approvedAmount));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Rejeita sinistro com motivo")
    public EntityModel<ClaimResponse> rejeitar(@PathVariable UUID id,
                                                @RequestParam Integer rejectionReasonId) {
        return toModel(claimService.rejeitar(id, rejectionReasonId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove sinistro (soft delete)")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        claimService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<ClaimResponse> toModel(ClaimResponse c) {
        return EntityModel.of(c,
                linkTo(methodOn(ClaimController.class).buscar(c.id())).withSelfRel(),
                linkTo(methodOn(ClaimController.class).listar(Pageable.unpaged())).withRel("claims"));
    }
}
