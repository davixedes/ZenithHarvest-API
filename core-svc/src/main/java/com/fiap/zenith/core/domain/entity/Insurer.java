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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Seguradora credenciada na plataforma Zenith Harvest. */
@Entity
@Table(name = "Insurer")
public class Insurer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Generated(event = EventType.INSERT)
    @Column(name = "Code", updatable = false, insertable = false)
    private Integer code;

    @Column(name = "CorporateName", nullable = false, length = 200)
    private String corporateName;

    @Column(name = "TradeName", length = 150)
    private String tradeName;

    @Column(name = "Cnpj", nullable = false, unique = true, length = 18)
    private String cnpj;

    @Column(name = "SusepCode", unique = true, length = 20)
    private String susepCode;

    @Column(name = "CommercialEmail", length = 150)
    private String commercialEmail;

    @Column(name = "Phone", length = 20)
    private String phone;

    @Column(name = "LogoUrl", length = 500)
    private String logoUrl;

    @Column(name = "AdminFeePct", precision = 5, scale = 2)
    private BigDecimal adminFeePct;

    @Column(name = "TakeRatePct", precision = 5, scale = 2)
    private BigDecimal takeRatePct;

    @Column(name = "Active", nullable = false)
    private Boolean active = true;

    @Column(name = "InsurerSituationId", nullable = false)
    private Integer insurerSituationId;

    @Column(name = "AccreditedAt")
    private LocalDate accreditedAt;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "EditedAt")
    private OffsetDateTime editedAt;

    @Column(name = "DeletedAt")
    private OffsetDateTime deletedAt;

    protected Insurer() {}

    public static Insurer create(String corporateName, String tradeName, String cnpj,
                                  String susepCode, String commercialEmail, String phone,
                                  String logoUrl, BigDecimal adminFeePct, BigDecimal takeRatePct,
                                  Integer insurerSituationId, LocalDate accreditedAt) {
        Insurer i = new Insurer();
        i.corporateName = corporateName;
        i.tradeName = tradeName;
        i.cnpj = cnpj;
        i.susepCode = susepCode;
        i.commercialEmail = commercialEmail;
        i.phone = phone;
        i.logoUrl = logoUrl;
        i.adminFeePct = adminFeePct != null ? adminFeePct : new BigDecimal("5.00");
        i.takeRatePct = takeRatePct != null ? takeRatePct : new BigDecimal("8.00");
        i.active = true;
        i.insurerSituationId = insurerSituationId;
        // operacional com DEFAULT no banco: se não vier, usa hoje (evita null no NOT NULL do INSERT)
        i.accreditedAt = accreditedAt != null ? accreditedAt : LocalDate.now();
        i.createdAt = OffsetDateTime.now();
        return i;
    }

    public UUID getId() { return id; }
    public Integer getCode() { return code; }
    public String getCorporateName() { return corporateName; }
    public String getTradeName() { return tradeName; }
    public String getCnpj() { return cnpj; }
    public String getSusepCode() { return susepCode; }
    public String getCommercialEmail() { return commercialEmail; }
    public String getPhone() { return phone; }
    public String getLogoUrl() { return logoUrl; }
    public BigDecimal getAdminFeePct() { return adminFeePct; }
    public BigDecimal getTakeRatePct() { return takeRatePct; }
    public Boolean getActive() { return active; }
    public Integer getInsurerSituationId() { return insurerSituationId; }
    public LocalDate getAccreditedAt() { return accreditedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getEditedAt() { return editedAt; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }

    public void setCorporateName(String corporateName) { this.corporateName = corporateName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public void setCommercialEmail(String commercialEmail) { this.commercialEmail = commercialEmail; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public void setAdminFeePct(BigDecimal adminFeePct) { this.adminFeePct = adminFeePct; }
    public void setTakeRatePct(BigDecimal takeRatePct) { this.takeRatePct = takeRatePct; }
    public void setActive(Boolean active) { this.active = active; }
    public void setInsurerSituationId(Integer insurerSituationId) { this.insurerSituationId = insurerSituationId; }
    public void setAccreditedAt(LocalDate accreditedAt) { this.accreditedAt = accreditedAt; }
    public void setEditedAt(OffsetDateTime editedAt) { this.editedAt = editedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
}
