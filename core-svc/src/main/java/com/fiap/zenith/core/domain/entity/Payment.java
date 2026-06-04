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
 * Pagamento PIX de indenização (ClaimId) ou cobrança de prêmio (PolicyId).
 * Constraint XOR no banco: exatamente um dos dois deve ser NOT NULL.
 */
@Entity
@Table(name = "Payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Generated(event = EventType.INSERT)
    @Column(name = "Code", updatable = false, insertable = false)
    private Integer code;

    @Column(name = "PaymentTypeId", nullable = false)
    private Integer paymentTypeId;

    @Column(name = "PaymentSituationId", nullable = false)
    private Integer paymentSituationId;

    @Column(name = "ClaimId")
    private UUID claimId;

    @Column(name = "PolicyId")
    private UUID policyId;

    @Column(name = "PaymentInvoiceId")
    private UUID paymentInvoiceId;

    @Column(name = "Amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "PixKey", length = 140)
    private String pixKey;

    @Column(name = "SentAt")
    private OffsetDateTime sentAt;

    @Column(name = "ConfirmedAt")
    private OffsetDateTime confirmedAt;

    @Column(name = "PspTransactionId", length = 100)
    private String pspTransactionId;

    @Column(name = "Attempts")
    private Short attempts = 0;

    @Column(name = "FailureReason", length = 500)
    private String failureReason;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "EditedAt")
    private OffsetDateTime editedAt;

    @Column(name = "DeletedAt")
    private OffsetDateTime deletedAt;

    protected Payment() {}

    public static Payment create(Integer paymentTypeId, Integer paymentSituationId,
                                  UUID claimId, UUID policyId, UUID paymentInvoiceId,
                                  BigDecimal amount, String pixKey) {
        Payment p = new Payment();
        p.paymentTypeId = paymentTypeId;
        p.paymentSituationId = paymentSituationId;
        p.claimId = claimId;
        p.policyId = policyId;
        p.paymentInvoiceId = paymentInvoiceId;
        p.amount = amount;
        p.pixKey = pixKey;
        p.attempts = 0;
        p.createdAt = OffsetDateTime.now();
        return p;
    }

    public UUID getId() { return id; }
    public Integer getCode() { return code; }
    public Integer getPaymentTypeId() { return paymentTypeId; }
    public Integer getPaymentSituationId() { return paymentSituationId; }
    public UUID getClaimId() { return claimId; }
    public UUID getPolicyId() { return policyId; }
    public UUID getPaymentInvoiceId() { return paymentInvoiceId; }
    public BigDecimal getAmount() { return amount; }
    public String getPixKey() { return pixKey; }
    public OffsetDateTime getSentAt() { return sentAt; }
    public OffsetDateTime getConfirmedAt() { return confirmedAt; }
    public String getPspTransactionId() { return pspTransactionId; }
    public Short getAttempts() { return attempts; }
    public String getFailureReason() { return failureReason; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getEditedAt() { return editedAt; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }

    public void setPaymentSituationId(Integer paymentSituationId) { this.paymentSituationId = paymentSituationId; }
    public void setSentAt(OffsetDateTime sentAt) { this.sentAt = sentAt; }
    public void setConfirmedAt(OffsetDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
    public void setPspTransactionId(String pspTransactionId) { this.pspTransactionId = pspTransactionId; }
    public void setAttempts(Short attempts) { this.attempts = attempts; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public void setEditedAt(OffsetDateTime editedAt) { this.editedAt = editedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
}
