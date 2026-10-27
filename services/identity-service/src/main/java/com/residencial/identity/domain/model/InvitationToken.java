package com.residencial.identity.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invitation_tokens", schema = "identity_schema")
public class InvitationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token", nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_used", nullable = false)
    private boolean isUsed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected InvitationToken() {
        // JPA constructor
    }

    public InvitationToken(UUID userId, String token, Instant expiresAt) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null.");
        }
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be empty.");
        }
        if (expiresAt == null || expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("ExpiresAt must be a valid future instant.");
        }
        this.userId = userId;
        this.token = token.trim();
        this.expiresAt = expiresAt;
        this.isUsed = false;
        this.createdAt = Instant.now();
    }

    public boolean isValid() {
        return !isUsed && Instant.now().isBefore(expiresAt);
    }

    public void markAsUsed() {
        this.isUsed = true;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
