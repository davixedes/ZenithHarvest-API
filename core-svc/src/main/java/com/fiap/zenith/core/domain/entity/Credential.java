package com.fiap.zenith.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Credenciais de acesso de um {@link User}. Domínio Users. O {@code Password} guarda o
 * <strong>hash BCrypt</strong> (nunca a senha em claro) e o {@code Secret} um valor aleatório
 * por usuário. Esta entidade NUNCA é exposta em DTO.
 */
@Entity
@Table(name = "Credential")
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id")
    private UUID id;

    @Column(name = "UserId", nullable = false)
    private UUID userId;

    @Column(name = "Password", nullable = false, length = 255)
    private String password;

    @Column(name = "Secret", nullable = false, length = 255)
    private String secret;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "EditedAt")
    private OffsetDateTime editedAt;

    @Column(name = "DeletedAt")
    private OffsetDateTime deletedAt;

    protected Credential() {
        // exigido pelo JPA
    }

    public static Credential create(UUID userId, String passwordHash, String secret) {
        Credential credential = new Credential();
        credential.userId = userId;
        credential.password = passwordHash;
        credential.secret = secret;
        credential.createdAt = OffsetDateTime.now();
        return credential;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(OffsetDateTime editedAt) {
        this.editedAt = editedAt;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
