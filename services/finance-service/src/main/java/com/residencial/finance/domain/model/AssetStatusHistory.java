package com.residencial.finance.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "asset_status_history", schema = "finance_schema")
public class AssetStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private FixedAsset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false, length = 30)
    private AssetStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private AssetStatus newStatus;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "changed_by_user_id")
    private UUID changedByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AssetStatusHistory() {
        // JPA constructor
    }

    public AssetStatusHistory(FixedAsset asset, AssetStatus previousStatus, AssetStatus newStatus,
                              String reason, UUID changedByUserId) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset cannot be null.");
        }
        if (previousStatus == null) {
            throw new IllegalArgumentException("Previous status cannot be null.");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Status change reason cannot be empty.");
        }

        this.id = UUID.randomUUID();
        this.asset = asset;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason.trim();
        this.changedByUserId = changedByUserId;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public FixedAsset getAsset() { return asset; }
    public AssetStatus getPreviousStatus() { return previousStatus; }
    public AssetStatus getNewStatus() { return newStatus; }
    public String getReason() { return reason; }
    public UUID getChangedByUserId() { return changedByUserId; }
    public Instant getCreatedAt() { return createdAt; }
}
