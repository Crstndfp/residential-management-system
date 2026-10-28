package com.residencial.amenities.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assembly_attendance", schema = "amenities_schema")
public class AssemblyAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "assembly_id", nullable = false)
    private UUID assemblyId;

    @Column(name = "house_id", nullable = false)
    private UUID houseId;

    @Column(name = "house_number", nullable = false, length = 20)
    private String houseNumber;

    @Column(name = "attended_by_user_id", nullable = false)
    private UUID attendedByUserId;

    @Column(name = "attended_by_name", nullable = false, length = 150)
    private String attendedByName;

    @Column(name = "is_present", nullable = false)
    private boolean isPresent;

    @Column(name = "checked_in_at", nullable = false)
    private Instant checkedInAt;

    protected AssemblyAttendance() {
    }

    public AssemblyAttendance(UUID id, UUID assemblyId, UUID houseId, String houseNumber,
                              UUID attendedByUserId, String attendedByName, boolean isPresent,
                              Instant checkedInAt) {
        if (assemblyId == null) {
            throw new IllegalArgumentException("Assembly ID cannot be null");
        }
        if (houseId == null) {
            throw new IllegalArgumentException("House ID cannot be null");
        }
        if (houseNumber == null || houseNumber.isBlank()) {
            throw new IllegalArgumentException("House number cannot be blank");
        }
        if (attendedByUserId == null) {
            throw new IllegalArgumentException("Attending user ID cannot be null");
        }
        if (attendedByName == null || attendedByName.isBlank()) {
            throw new IllegalArgumentException("Attending user name cannot be blank");
        }

        this.id = id != null ? id : UUID.randomUUID();
        this.assemblyId = assemblyId;
        this.houseId = houseId;
        this.houseNumber = houseNumber;
        this.attendedByUserId = attendedByUserId;
        this.attendedByName = attendedByName;
        this.isPresent = isPresent;
        this.checkedInAt = checkedInAt != null ? checkedInAt : Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAssemblyId() {
        return assemblyId;
    }

    public UUID getHouseId() {
        return houseId;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public UUID getAttendedByUserId() {
        return attendedByUserId;
    }

    public String getAttendedByName() {
        return attendedByName;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public Instant getCheckedInAt() {
        return checkedInAt;
    }
}
