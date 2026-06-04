package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.UpdateUserRequest;
import com.fiap.zenith.core.application.dto.UserResponse;
import com.fiap.zenith.core.application.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Endpoints de usuário (protegidos por JWT). {@code /me} resolve o usuário pelo {@code sub}
 * do token. Respostas com HATEOAS.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public EntityModel<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return toModel(userService.buscarPorId(UUID.fromString(jwt.getSubject())));
    }

    @GetMapping("/{id}")
    public EntityModel<UserResponse> buscar(@PathVariable UUID id) {
        return toModel(userService.buscarPorId(id));
    }

    @GetMapping
    public CollectionModel<EntityModel<UserResponse>> listar(@PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> page = userService.listar(pageable);
        List<EntityModel<UserResponse>> users = page.map(this::toModel).getContent();
        return CollectionModel.of(users,
                linkTo(methodOn(UserController.class).listar(pageable)).withSelfRel());
    }

    @PutMapping("/{id}")
    public EntityModel<UserResponse> atualizar(@PathVariable UUID id,
                                               @Valid @RequestBody UpdateUserRequest req) {
        return toModel(userService.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        userService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<UserResponse> toModel(UserResponse user) {
        return EntityModel.of(user,
                linkTo(methodOn(UserController.class).buscar(user.id())).withSelfRel(),
                linkTo(methodOn(UserController.class).listar(Pageable.unpaged())).withRel("users"));
    }
}
