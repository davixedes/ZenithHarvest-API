package com.fiap.zenith.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/** Lookup: tipos de evento causador de sinistro (seca, geada, granizo...). */
@Entity
@Table(name = "ClaimEventType")
public class ClaimEventType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Description", nullable = false, length = 150)
    private String description;

    @Column(name = "RequiresPhoto", nullable = false)
    private Boolean requiresPhoto = false;

    @Column(name = "Active", nullable = false)
    private Boolean active = true;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "DeletedAt")
    private OffsetDateTime deletedAt;

    protected ClaimEventType() {}

    public Integer getId() { return id; }
    public String getDescription() { return description; }
    public Boolean getRequiresPhoto() { return requiresPhoto; }
    public Boolean getActive() { return active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
}
