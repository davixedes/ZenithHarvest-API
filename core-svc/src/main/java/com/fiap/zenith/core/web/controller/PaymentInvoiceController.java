package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.CreatePaymentInvoiceRequest;
import com.fiap.zenith.core.application.dto.PaymentInvoiceResponse;
import com.fiap.zenith.core.application.dto.UpdatePaymentInvoiceRequest;
import com.fiap.zenith.core.application.service.PaymentInvoiceService;
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
@RequestMapping("/api/payment-invoices")
@Tag(name = "Payment Invoices", description = "Faturas de cobrança de prêmio")
public class PaymentInvoiceController {

    private final PaymentInvoiceService paymentInvoiceService;

    public PaymentInvoiceController(PaymentInvoiceService paymentInvoiceService) {
        this.paymentInvoiceService = paymentInvoiceService;
    }

    @PostMapping
    @Operation(summary = "Cria fatura de cobrança")
    public ResponseEntity<EntityModel<PaymentInvoiceResponse>> criar(
            @Valid @RequestBody CreatePaymentInvoiceRequest req) {
        PaymentInvoiceResponse created = paymentInvoiceService.criar(req);
        return ResponseEntity
                .created(linkTo(methodOn(PaymentInvoiceController.class).buscar(created.id())).toUri())
                .body(toModel(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca fatura por ID")
    public EntityModel<PaymentInvoiceResponse> buscar(@PathVariable UUID id) {
        return toModel(paymentInvoiceService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista faturas ativas, com filtro opcional por usuário")
    public CollectionModel<EntityModel<PaymentInvoiceResponse>> listar(@PageableDefault(size = 20) Pageable pageable,
                                                                       @RequestParam(required = false) UUID userId) {
        Page<PaymentInvoiceResponse> page = paymentInvoiceService.listar(userId, pageable);
        List<EntityModel<PaymentInvoiceResponse>> items = page.map(this::toModel).getContent();
        return CollectionModel.of(items,
                linkTo(methodOn(PaymentInvoiceController.class).listar(pageable, userId)).withSelfRel());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza valores e vencimento da fatura")
    public EntityModel<PaymentInvoiceResponse> atualizar(@PathVariable UUID id,
                                                         @Valid @RequestBody UpdatePaymentInvoiceRequest req) {
        return toModel(paymentInvoiceService.atualizar(id, req));
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "Marca fatura como paga")
    public EntityModel<PaymentInvoiceResponse> pagar(@PathVariable UUID id) {
        return toModel(paymentInvoiceService.marcarComoPaga(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativa fatura")
    public ResponseEntity<Void> desativar(@PathVariable UUID id) {
        paymentInvoiceService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<PaymentInvoiceResponse> toModel(PaymentInvoiceResponse invoice) {
        return EntityModel.of(invoice,
                linkTo(methodOn(PaymentInvoiceController.class).buscar(invoice.id())).withSelfRel(),
                linkTo(methodOn(PaymentInvoiceController.class).listar(Pageable.unpaged(), invoice.userId()))
                        .withRel("payment-invoices"));
    }
}
