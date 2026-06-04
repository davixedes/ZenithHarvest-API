package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.CreateInsuranceRequest;
import com.fiap.zenith.core.application.dto.InsuranceResponse;
import com.fiap.zenith.core.application.service.InsuranceService;
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
@RequestMapping("/api/insurances")
@Tag(name = "Insurances", description = "Produtos de seguro paramétrico")
public class InsuranceController {

    private final InsuranceService insuranceService;

    public InsuranceController(InsuranceService insuranceService) {
        this.insuranceService = insuranceService;
    }

    @PostMapping
    @Operation(summary = "Cria produto de seguro")
    public ResponseEntity<EntityModel<InsuranceResponse>> criar(@Valid @RequestBody CreateInsuranceRequest req) {
        InsuranceResponse created = insuranceService.criar(req);
        return ResponseEntity
                .created(linkTo(methodOn(InsuranceController.class).buscar(created.id())).toUri())
                .body(toModel(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca produto de seguro por ID")
    public EntityModel<InsuranceResponse> buscar(@PathVariable UUID id) {
        return toModel(insuranceService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista produtos de seguro (resultado cacheado)")
    public CollectionModel<EntityModel<InsuranceResponse>> listar(@PageableDefault(size = 20) Pageable pageable) {
        Page<InsuranceResponse> page = insuranceService.listar(pageable);
        List<EntityModel<InsuranceResponse>> items = page.map(this::toModel).getContent();
        return CollectionModel.of(items,
                linkTo(methodOn(InsuranceController.class).listar(pageable)).withSelfRel());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza produto de seguro")
    public EntityModel<InsuranceResponse> atualizar(@PathVariable UUID id,
                                                     @Valid @RequestBody CreateInsuranceRequest req) {
        return toModel(insuranceService.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove produto de seguro (soft delete)")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        insuranceService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<InsuranceResponse> toModel(InsuranceResponse ins) {
        return EntityModel.of(ins,
                linkTo(methodOn(InsuranceController.class).buscar(ins.id())).withSelfRel(),
                linkTo(methodOn(InsuranceController.class).listar(Pageable.unpaged())).withRel("insurances"));
    }
}
