package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.UserResponse;
import com.fiap.zenith.core.application.dto.auth.AddressRequest;
import com.fiap.zenith.core.application.dto.auth.AuthResponse;
import com.fiap.zenith.core.application.dto.auth.LoginRequest;
import com.fiap.zenith.core.application.dto.auth.RegisterRequest;
import com.fiap.zenith.core.application.exception.DuplicateResourceException;
import com.fiap.zenith.core.application.mapper.UserMapper;
import com.fiap.zenith.core.domain.entity.Address;
import com.fiap.zenith.core.domain.entity.Credential;
import com.fiap.zenith.core.domain.entity.User;
import com.fiap.zenith.core.domain.repository.AddressRepository;
import com.fiap.zenith.core.domain.repository.CredentialRepository;
import com.fiap.zenith.core.domain.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Regras de autenticação: cadastro de usuário (com endereço e credencial BCrypt) e login
 * (validação + emissão de JWT + trilha de acesso).
 */
@Service
public class AuthService {

    private static final String ROLE_USER = "ROLE_USER";
    private static final String ACTION_LOGIN_SUCCESS = "LOGIN_SUCCESS";
    private static final String ACTION_LOGIN_FAILURE = "LOGIN_FAILURE";

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AccessLogService accessLogService;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository,
                       CredentialRepository credentialRepository,
                       AddressRepository addressRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AccessLogService accessLogService,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.accessLogService = accessLogService;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse registrar(RegisterRequest req) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(req.email())) {
            throw new DuplicateResourceException("E-mail já cadastrado: " + req.email());
        }
        if (userRepository.existsByCpfAndDeletedAtIsNull(req.cpf())) {
            throw new DuplicateResourceException("CPF já cadastrado: " + req.cpf());
        }

        UUID addressId = null;
        if (req.address() != null) {
            AddressRequest a = req.address();
            Address address = addressRepository.save(Address.create(
                    a.street(), a.number(), a.neighboor(), a.city(),
                    a.complement(), a.postalCode(), a.uf(), a.country()));
            addressId = address.getId();
        }

        User user = userRepository.save(User.create(
                req.cpf(), req.name(), req.lastName(), req.email(), req.phone(), addressId));

        String passwordHash = passwordEncoder.encode(req.password());
        String secret = UUID.randomUUID().toString();
        credentialRepository.save(Credential.create(user.getId(), passwordHash, secret));

        return userMapper.toResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req, String ip, String userAgent) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(req.email()).orElse(null);
        if (user == null) {
            accessLogService.record(null, ACTION_LOGIN_FAILURE, false, ip, userAgent, "E-mail não encontrado");
            throw new BadCredentialsException("Credenciais inválidas");
        }

        Credential credential = credentialRepository.findByUserIdAndDeletedAtIsNull(user.getId()).orElse(null);
        if (credential == null || !passwordEncoder.matches(req.password(), credential.getPassword())) {
            accessLogService.record(user.getId(), ACTION_LOGIN_FAILURE, false, ip, userAgent, "Senha inválida");
            throw new BadCredentialsException("Credenciais inválidas");
        }

        String token = jwtService.generateToken(user, List.of(ROLE_USER));
        user.setLastLoginAt(OffsetDateTime.now());
        accessLogService.record(user.getId(), ACTION_LOGIN_SUCCESS, true, ip, userAgent, null);

        return AuthResponse.bearer(token, jwtService.getExpirationSeconds(), user.getId(), user.getEmail());
    }
}
