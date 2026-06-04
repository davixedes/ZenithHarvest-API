package com.fiap.zenith.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Produtor rural (ou operador) do sistema. Domínio Users. Mapeia a tabela {@code "User"}
 * — palavra reservada no Postgres, por isso o uso obrigatório de aspas (já garantido por
 * {@code globally_quoted_identifiers}).
 *
 * <p>{@code AddressId} é mantido como ID cru (a entidade {@link Address} é gravada à parte
 * pelo serviço de cadastro). A senha NÃO fica aqui — vive em {@link Credential}.</p>
 */
@Entity
@Table(name = "User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id")
    private UUID id;

    @Column(name = "Cpf", nullable = false, length = 18, unique = true)
    private String cpf;

    @Column(name = "Name", nullable = false, length = 150)
    private String name;

    @Column(name = "LastName", nullable = false, length = 150)
    private String lastName;

    @Column(name = "AddressId")
    private UUID addressId;

    @Column(name = "Email", nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "Phone", nullable = false, length = 20)
    private String phone;

    /** Número "bonito" gerado pelo banco (IDENTITY, 1 em 1). Somente leitura. */
    @Generated(event = EventType.INSERT)
    @Column(name = "Code", insertable = false, updatable = false)
    private Integer code;

    @Column(name = "LastLoginAt")
    private OffsetDateTime lastLoginAt;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "EditedAt")
    private OffsetDateTime editedAt;

    @Column(name = "UpdatedAt")
    private OffsetDateTime updatedAt;

    @Column(name = "DeletedAt")
    private OffsetDateTime deletedAt;

    protected User() {
        // exigido pelo JPA
    }

    public static User create(String cpf, String name, String lastName, String email,
                              String phone, UUID addressId) {
        User user = new User();
        user.cpf = cpf;
        user.name = name;
        user.lastName = lastName;
        user.email = email;
        user.phone = phone;
        user.addressId = addressId;
        user.createdAt = OffsetDateTime.now();
        return user;
    }

    public UUID getId() {
        return id;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public UUID getAddressId() {
        return addressId;
    }

    public void setAddressId(UUID addressId) {
        this.addressId = addressId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getCode() {
        return code;
    }

    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(OffsetDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
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

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
