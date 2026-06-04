package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.CreateInsuranceQuoteRequest;
import com.fiap.zenith.core.application.dto.InsuranceQuoteResponse;
import com.fiap.zenith.core.application.service.InsuranceQuoteService;
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
@RequestMapping("/api/quotes")
@Tag(name = "InsuranceQuotes", description = "Cotações de seguro paramétrico")
public class InsuranceQuoteController {

    private final InsuranceQuoteService quoteService;

    public InsuranceQuoteController(InsuranceQuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping
    @Operation(summary = "Gera uma cotação de seguro")
    public ResponseEntity<EntityModel<InsuranceQuoteResponse>> criar(
            @Valid @RequestBody CreateInsuranceQuoteRequest req) {
        InsuranceQuoteResponse created = quoteService.criar(req);
        return ResponseEntity
                .created(linkTo(methodOn(InsuranceQuoteController.class).buscar(created.id())).toUri())
                .body(toModel(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca cotação por ID")
    public EntityModel<InsuranceQuoteResponse> buscar(@PathVariable UUID id) {
        return toModel(quoteService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista cotações")
    public CollectionModel<EntityModel<InsuranceQuoteResponse>> listar(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<InsuranceQuoteResponse> page = quoteService.listar(pageable);
        List<EntityModel<InsuranceQuoteResponse>> items = page.map(this::toModel).getContent();
        return CollectionModel.of(items,
                linkTo(methodOn(InsuranceQuoteController.class).listar(pageable)).withSelfRel());
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "Aceita uma cotação (muda situação para Aceita)")
    public EntityModel<InsuranceQuoteResponse> aceitar(@PathVariable UUID id) {
        return toModel(quoteService.aceitar(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove cotação")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        quoteService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<InsuranceQuoteResponse> toModel(InsuranceQuoteResponse q) {
        return EntityModel.of(q,
                linkTo(methodOn(InsuranceQuoteController.class).buscar(q.id())).withSelfRel(),
                linkTo(methodOn(InsuranceQuoteController.class).listar(Pageable.unpaged())).withRel("quotes"));
    }
}
