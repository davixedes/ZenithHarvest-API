package com.fiap.zenith.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/** Lookup: situações de pagamento (Pendente → Processando → Confirmado/Falhou/Estornado). */
@Entity
@Table(name = "PaymentSituation")
public class PaymentSituation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Description", nullable = false, length = 100)
    private String description;

    @Column(name = "IsTerminal", nullable = false)
    private Boolean isTerminal = false;

    @Column(name = "IsSuccess", nullable = false)
    private Boolean isSuccess = false;

    @Column(name = "Active", nullable = false)
    private Boolean active = true;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected PaymentSituation() {}

    public Integer getId() { return id; }
    public String getDescription() { return description; }
    public Boolean getIsTerminal() { return isTerminal; }
    public Boolean getIsSuccess() { return isSuccess; }
    public Boolean getActive() { return active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
