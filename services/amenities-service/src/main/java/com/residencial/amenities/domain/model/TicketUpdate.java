package com.residencial.amenities.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_updates", schema = "amenities_schema")
public class TicketUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Column(name = "author_user_id", nullable = false)
    private UUID authorUserId;

    @Column(name = "author_name", nullable = false, length = 150)
    private String authorName;

    @Column(name = "author_role", nullable = false, length = 30)
    private String authorRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false, length = 30)
    private TicketStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private TicketStatus newStatus;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TicketUpdate() {
    }

    public TicketUpdate(UUID id, UUID ticketId, UUID authorUserId, String authorName,
                        String authorRole, TicketStatus previousStatus, TicketStatus newStatus,
                        String notes, Instant createdAt) {
        if (ticketId == null) {
            throw new IllegalArgumentException("Ticket ID cannot be null");
        }
        if (authorUserId == null) {
            throw new IllegalArgumentException("Author user ID cannot be null");
        }
        if (previousStatus == null || newStatus == null) {
            throw new IllegalArgumentException("Ticket status change must have both previous and new statuses");
        }
        if (notes == null || notes.isBlank()) {
            throw new IllegalArgumentException("Update notes cannot be blank");
        }

        this.id = id != null ? id : UUID.randomUUID();
        this.ticketId = ticketId;
        this.authorUserId = authorUserId;
        this.authorName = authorName != null ? authorName : "Staff";
        this.authorRole = authorRole != null ? authorRole : "ADMIN";
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.notes = notes;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public UUID getAuthorUserId() {
        return authorUserId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public TicketStatus getPreviousStatus() {
        return previousStatus;
    }

    public TicketStatus getNewStatus() {
        return newStatus;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
