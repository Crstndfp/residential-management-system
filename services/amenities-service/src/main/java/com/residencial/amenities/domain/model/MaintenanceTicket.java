package com.residencial.amenities.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "maintenance_tickets", schema = "amenities_schema")
public class MaintenanceTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String folio;

    @Column(name = "reporter_user_id", nullable = false)
    private UUID reporterUserId;

    @Column(name = "reporter_name", nullable = false, length = 150)
    private String reporterName;

    @Column(name = "reporter_role", nullable = false, length = 30)
    private String reporterRole;

    @Column(name = "house_id", nullable = false)
    private UUID houseId;

    @Column(name = "house_number", nullable = false, length = 20)
    private String houseNumber;

    @Column(nullable = false, length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TicketCategory category;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketPriority priority;

    @Column(name = "assigned_technician", length = 150)
    private String assignedTechnician;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    protected MaintenanceTicket() {
    }

    public MaintenanceTicket(UUID id, String folio, UUID reporterUserId, String reporterName,
                             String reporterRole, UUID houseId, String houseNumber,
                             String title, TicketCategory category, String location,
                             String description, String photoUrl, TicketStatus status,
                             TicketPriority priority, String assignedTechnician,
                             Instant createdAt, Instant updatedAt, Instant closedAt) {
        if (folio == null || folio.isBlank()) {
            throw new IllegalArgumentException("Ticket folio cannot be blank");
        }
        if (reporterUserId == null) {
            throw new IllegalArgumentException("Reporter user ID cannot be null");
        }
        if (houseId == null) {
            throw new IllegalArgumentException("House ID cannot be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Ticket title cannot be blank");
        }
        if (category == null) {
            throw new IllegalArgumentException("Ticket category cannot be null");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Ticket location cannot be blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Ticket description cannot be blank");
        }

        this.id = id != null ? id : UUID.randomUUID();
        this.folio = folio;
        this.reporterUserId = reporterUserId;
        this.reporterName = reporterName != null ? reporterName : "Anonymous Resident";
        this.reporterRole = reporterRole != null ? reporterRole : "RESIDENT";
        this.houseId = houseId;
        this.houseNumber = houseNumber != null ? houseNumber : "N/A";
        this.title = title;
        this.category = category;
        this.location = location;
        this.description = description;
        this.photoUrl = photoUrl;
        this.status = status != null ? status : TicketStatus.OPEN;
        this.priority = priority != null ? priority : TicketPriority.MEDIUM;
        this.assignedTechnician = assignedTechnician;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.closedAt = closedAt;
    }

    public void updateStatus(TicketStatus newStatus, String technician) {
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
        if (this.status == TicketStatus.CLOSED && newStatus != TicketStatus.CLOSED) {
            throw new IllegalStateException("Cannot change status of an already closed ticket");
        }

        this.status = newStatus;
        if (technician != null && !technician.isBlank()) {
            this.assignedTechnician = technician;
        }
        this.updatedAt = Instant.now();

        if (newStatus == TicketStatus.CLOSED || newStatus == TicketStatus.RESOLVED) {
            this.closedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getFolio() {
        return folio;
    }

    public UUID getReporterUserId() {
        return reporterUserId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public String getReporterRole() {
        return reporterRole;
    }

    public UUID getHouseId() {
        return houseId;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getTitle() {
        return title;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public String getAssignedTechnician() {
        return assignedTechnician;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }
}
