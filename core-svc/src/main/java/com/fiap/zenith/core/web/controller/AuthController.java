package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.UserResponse;
import com.fiap.zenith.core.application.dto.auth.AuthResponse;
import com.fiap.zenith.core.application.dto.auth.LoginRequest;
import com.fiap.zenith.core.application.dto.auth.RegisterRequest;
import com.fiap.zenith.core.application.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos de autenticação. Cadastro e login (emissão de JWT).
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        UserResponse created = authService.registrar(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        return authService.login(req, http.getRemoteAddr(), http.getHeader("User-Agent"));
    }
}
