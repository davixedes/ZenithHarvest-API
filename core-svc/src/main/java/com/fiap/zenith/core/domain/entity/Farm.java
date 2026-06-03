package com.fiap.zenith.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Propriedade rural (fazenda) de um produtor. Domínio Farm.
 *
 * <p>Mapeia a tabela {@code "Farm"}. FKs estruturais ({@code UserId}, {@code BiomeId}) são
 * mantidas como identificadores crus enquanto as entidades {@code User}/{@code Biome} ainda
 * não existem no core-svc — quando forem criadas, trocar por {@code @ManyToOne} LAZY.</p>
 *
 * <p>NOT NULL conservador: só o que existe na criação da linha. Campos operacionais
 * ({@code propertyType}, {@code polygonWkt}, {@code biomeId}, {@code editedAt}, {@code deletedAt})
 * são nullable.</p>
 */
@Entity
@Table(name = "Farm")
public class Farm {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id")
    private UUID id;

    @Column(name = "UserId", nullable = false)
    private UUID userId;

    @Column(name = "Name", nullable = false, length = 150)
    private String name;

    /** Número "bonito" gerado pelo banco (IDENTITY, 1 em 1). Somente leitura. */
    @Generated(event = EventType.INSERT)
    @Column(name = "Code", insertable = false, updatable = false)
    private Integer code;

    @Column(name = "CarRegistration", nullable = false, length = 50)
    private String carRegistration;

    @Column(name = "Nirf", nullable = false, length = 20)
    private String nirf;

    @Column(name = "Latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "Longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "TotalAreaHectares", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAreaHectares;

    @Column(name = "State", nullable = false, length = 2, columnDefinition = "char(2)")
    private String state;

    @Column(name = "BiomeId")
    private Integer biomeId;

    @Column(name = "PropertyType", length = 30)
    private String propertyType;

    @Column(name = "PolygonWkt", columnDefinition = "text")
    private String polygonWkt;

    @Column(name = "Active", nullable = false)
    private Boolean active = true;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "EditedAt")
    private OffsetDateTime editedAt;

    @Column(name = "DeletedAt")
    private OffsetDateTime deletedAt;

    protected Farm() {
        // exigido pelo JPA
    }

    /**
     * Cria uma nova fazenda já com os defaults de criação ({@code active = true},
     * {@code createdAt = agora}). {@code code}, {@code id} e timestamps de transição são
     * preenchidos pelo banco/fluxo. Mantém o construtor sem-args protegido para o JPA.
     */
    public static Farm create(UUID userId, String name, String carRegistration, String nirf,
                              BigDecimal latitude, BigDecimal longitude, BigDecimal totalAreaHectares,
                              String state, Integer biomeId, String propertyType, String polygonWkt) {
        Farm farm = new Farm();
        farm.userId = userId;
        farm.name = name;
        farm.carRegistration = carRegistration;
        farm.nirf = nirf;
        farm.latitude = latitude;
        farm.longitude = longitude;
        farm.totalAreaHectares = totalAreaHectares;
        farm.state = state;
        farm.biomeId = biomeId;
        farm.propertyType = propertyType;
        farm.polygonWkt = polygonWkt;
        farm.active = true;
        farm.createdAt = OffsetDateTime.now();
        return farm;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getCarRegistration() {
        return carRegistration;
    }

    public void setCarRegistration(String carRegistration) {
        this.carRegistration = carRegistration;
    }

    public String getNirf() {
        return nirf;
    }

    public void setNirf(String nirf) {
        this.nirf = nirf;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getTotalAreaHectares() {
        return totalAreaHectares;
    }

    public void setTotalAreaHectares(BigDecimal totalAreaHectares) {
        this.totalAreaHectares = totalAreaHectares;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getBiomeId() {
        return biomeId;
    }

    public void setBiomeId(Integer biomeId) {
        this.biomeId = biomeId;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getPolygonWkt() {
        return polygonWkt;
    }

    public void setPolygonWkt(String polygonWkt) {
        this.polygonWkt = polygonWkt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
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
