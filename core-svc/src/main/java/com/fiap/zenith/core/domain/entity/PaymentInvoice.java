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

/**
 * Fatura de cobrança de prêmios. Sem soft-delete por timestamp — usa {@code Active}
 * (faturas não são apagadas, são desativadas).
 */
@Entity
@Table(name = "PaymentInvoice")
public class PaymentInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Generated(event = EventType.INSERT)
    @Column(name = "Code", updatable = false, insertable = false)
    private Integer code;

    @Column(name = "InvoiceNumber", nullable = false, unique = true, length = 30)
    private String invoiceNumber;

    @Column(name = "UserId", nullable = false)
    private UUID userId;

    @Column(name = "InsurerId")
    private UUID insurerId;

    @Column(name = "TotalAmount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "DueDate", nullable = false)
    private LocalDate dueDate;

    @Column(name = "IssuedAt", nullable = false, updatable = false)
    private OffsetDateTime issuedAt;

    @Column(name = "PaidAt")
    private OffsetDateTime paidAt;

    @Column(name = "Active", nullable = false)
    private Boolean active = true;

    protected PaymentInvoice() {}

    public static PaymentInvoice create(String invoiceNumber, UUID userId, UUID insurerId,
                                         BigDecimal totalAmount, LocalDate dueDate) {
        PaymentInvoice invoice = new PaymentInvoice();
        invoice.invoiceNumber = invoiceNumber;
        invoice.userId = userId;
        invoice.insurerId = insurerId;
        invoice.totalAmount = totalAmount;
        invoice.dueDate = dueDate;
        invoice.issuedAt = OffsetDateTime.now();
        invoice.active = true;
        return invoice;
    }

    public UUID getId() { return id; }
    public Integer getCode() { return code; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public UUID getUserId() { return userId; }
    public UUID getInsurerId() { return insurerId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDate getDueDate() { return dueDate; }
    public OffsetDateTime getIssuedAt() { return issuedAt; }
    public OffsetDateTime getPaidAt() { return paidAt; }
    public Boolean getActive() { return active; }

    public void setInsurerId(UUID insurerId) { this.insurerId = insurerId; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setPaidAt(OffsetDateTime paidAt) { this.paidAt = paidAt; }
    public void setActive(Boolean active) { this.active = active; }
}
