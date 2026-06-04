package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.AuditLogResponse;
import com.fiap.zenith.core.application.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit Logs", description = "Auditoria cross-cutting do sistema")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca audit log por ID")
    public EntityModel<AuditLogResponse> buscar(@PathVariable UUID id) {
        return toModel(auditLogService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista audit logs, com filtro opcional por usuário")
    public CollectionModel<EntityModel<AuditLogResponse>> listar(@PageableDefault(size = 20) Pageable pageable,
                                                                 @RequestParam(required = false) UUID userId) {
        Page<AuditLogResponse> page = auditLogService.listar(userId, pageable);
        List<EntityModel<AuditLogResponse>> items = page.map(this::toModel).getContent();
        return CollectionModel.of(items,
                linkTo(methodOn(AuditLogController.class).listar(pageable, userId)).withSelfRel());
    }

    private EntityModel<AuditLogResponse> toModel(AuditLogResponse log) {
        return EntityModel.of(log,
                linkTo(methodOn(AuditLogController.class).buscar(log.id())).withSelfRel(),
                linkTo(methodOn(AuditLogController.class).listar(Pageable.unpaged(), log.userId()))
                        .withRel("audit-logs"));
    }
}
