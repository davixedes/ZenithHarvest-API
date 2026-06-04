package com.fiap.zenith.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/** Lookup: severidades de alerta preventivo com cor visual e flags de notificação. */
@Entity
@Table(name = "AlertSeverity")
public class AlertSeverity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Description", nullable = false, length = 100)
    private String description;

    @Column(name = "Level", nullable = false)
    private Integer level;

    @Column(name = "ColorHex", length = 7)
    private String colorHex;

    @Column(name = "PushNotify", nullable = false)
    private Boolean pushNotify = false;

    @Column(name = "SmsNotify", nullable = false)
    private Boolean smsNotify = false;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected AlertSeverity() {}

    public Integer getId() { return id; }
    public String getDescription() { return description; }
    public Integer getLevel() { return level; }
    public String getColorHex() { return colorHex; }
    public Boolean getPushNotify() { return pushNotify; }
    public Boolean getSmsNotify() { return smsNotify; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
