package com.fiap.zenith.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/** Lookup: situações do ciclo de vida de uma cotação de seguro. */
@Entity
@Table(name = "InsuranceQuoteSituation")
public class InsuranceQuoteSituation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Description", nullable = false, length = 100)
    private String description;

    @Column(name = "IsTerminal", nullable = false)
    private Boolean isTerminal = false;

    @Column(name = "Active", nullable = false)
    private Boolean active = true;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected InsuranceQuoteSituation() {}

    public Integer getId() { return id; }
    public String getDescription() { return description; }
    public Boolean getIsTerminal() { return isTerminal; }
    public Boolean getActive() { return active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
