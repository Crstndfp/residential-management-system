package com.residencial.amenities.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assemblies", schema = "amenities_schema")
public class Assembly {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "assembly_type", nullable = false, length = 30)
    private AssemblyType assemblyType;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String agenda;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AssemblyStatus status;

    @Column(name = "total_residential_houses", nullable = false)
    private int totalResidentialHouses;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Assembly() {
    }

    public Assembly(UUID id, String title, AssemblyType assemblyType, Instant scheduledAt,
                    String agenda, AssemblyStatus status, int totalResidentialHouses,
                    Instant createdAt, Instant updatedAt) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Assembly title cannot be blank");
        }
        if (assemblyType == null) {
            throw new IllegalArgumentException("Assembly type must be provided");
        }
        if (scheduledAt == null) {
            throw new IllegalArgumentException("Assembly scheduled time cannot be null");
        }
        if (agenda == null || agenda.isBlank()) {
            throw new IllegalArgumentException("Assembly agenda cannot be blank");
        }
        if (totalResidentialHouses <= 0) {
            throw new IllegalArgumentException("Total residential houses must be greater than zero");
        }

        this.id = id != null ? id : UUID.randomUUID();
        this.title = title;
        this.assemblyType = assemblyType;
        this.scheduledAt = scheduledAt;
        this.agenda = agenda;
        this.status = status != null ? status : AssemblyStatus.CONVOKED;
        this.totalResidentialHouses = totalResidentialHouses;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public void startAssembly() {
        if (this.status == AssemblyStatus.CONCLUDED || this.status == AssemblyStatus.CANCELLED) {
            throw new IllegalStateException("Cannot start an assembly that is " + this.status);
        }
        this.status = AssemblyStatus.IN_PROGRESS;
        this.updatedAt = Instant.now();
    }

    public void concludeAssembly() {
        if (this.status == AssemblyStatus.CANCELLED) {
            throw new IllegalStateException("Cannot conclude a cancelled assembly");
        }
        this.status = AssemblyStatus.CONCLUDED;
        this.updatedAt = Instant.now();
    }

    public void cancelAssembly() {
        if (this.status == AssemblyStatus.CONCLUDED) {
            throw new IllegalStateException("Cannot cancel a concluded assembly");
        }
        this.status = AssemblyStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public AssemblyType getAssemblyType() {
        return assemblyType;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public String getAgenda() {
        return agenda;
    }

    public AssemblyStatus getStatus() {
        return status;
    }

    public int getTotalResidentialHouses() {
        return totalResidentialHouses;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
