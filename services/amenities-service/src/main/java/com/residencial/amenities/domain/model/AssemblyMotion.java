package com.residencial.amenities.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assembly_motions", schema = "amenities_schema")
public class AssemblyMotion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "assembly_id", nullable = false)
    private UUID assemblyId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MotionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MotionVerdict verdict;

    @Column(name = "opened_at")
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AssemblyMotion() {
    }

    public AssemblyMotion(UUID id, UUID assemblyId, String title, String description,
                          MotionStatus status, MotionVerdict verdict,
                          Instant openedAt, Instant closedAt, Instant createdAt) {
        if (assemblyId == null) {
            throw new IllegalArgumentException("Assembly ID cannot be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Motion title cannot be blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Motion description cannot be blank");
        }

        this.id = id != null ? id : UUID.randomUUID();
        this.assemblyId = assemblyId;
        this.title = title;
        this.description = description;
        this.status = status != null ? status : MotionStatus.PENDING;
        this.verdict = verdict;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public void open() {
        if (this.status == MotionStatus.CLOSED) {
            throw new IllegalStateException("Cannot reopen a closed motion");
        }
        this.status = MotionStatus.OPEN;
        this.openedAt = Instant.now();
    }

    public void close(MotionVerdict finalVerdict) {
        if (this.status != MotionStatus.OPEN) {
            throw new IllegalStateException("Only open motions can be closed");
        }
        if (finalVerdict == null) {
            throw new IllegalArgumentException("Verdict must be specified when closing motion");
        }
        this.status = MotionStatus.CLOSED;
        this.verdict = finalVerdict;
        this.closedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAssemblyId() {
        return assemblyId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public MotionStatus getStatus() {
        return status;
    }

    public MotionVerdict getVerdict() {
        return verdict;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
