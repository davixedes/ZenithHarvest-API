package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.CreatePaymentRequest;
import com.fiap.zenith.core.application.dto.PaymentResponse;
import com.fiap.zenith.core.application.service.PaymentService;
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
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Pagamentos PIX de indenização e prêmios")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Cria pagamento")
    public ResponseEntity<EntityModel<PaymentResponse>> criar(@Valid @RequestBody CreatePaymentRequest req) {
        PaymentResponse created = paymentService.criar(req);
        return ResponseEntity
                .created(linkTo(methodOn(PaymentController.class).buscar(created.id())).toUri())
                .body(toModel(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca pagamento por ID")
    public EntityModel<PaymentResponse> buscar(@PathVariable UUID id) {
        return toModel(paymentService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista pagamentos")
    public CollectionModel<EntityModel<PaymentResponse>> listar(@PageableDefault(size = 20) Pageable pageable) {
        Page<PaymentResponse> page = paymentService.listar(pageable);
        List<EntityModel<PaymentResponse>> items = page.map(this::toModel).getContent();
        return CollectionModel.of(items,
                linkTo(methodOn(PaymentController.class).listar(pageable)).withSelfRel());
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirma pagamento PIX (o pspTransactionId é gerado pelo provedor/PSP)")
    public EntityModel<PaymentResponse> confirmar(@PathVariable UUID id) {
        return toModel(paymentService.confirmar(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove pagamento (soft delete)")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        paymentService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<PaymentResponse> toModel(PaymentResponse p) {
        return EntityModel.of(p,
                linkTo(methodOn(PaymentController.class).buscar(p.id())).withSelfRel(),
                linkTo(methodOn(PaymentController.class).listar(Pageable.unpaged())).withRel("payments"));
    }
}
