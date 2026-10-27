package com.residencial.identity.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "identity_schema")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id")
    private House house;

    @Embedded
    private IdentificationDocument identificationDocument;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "residential_role", nullable = false, length = 30)
    private ResidentialRole residentialRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_role", nullable = false, length = 30)
    private BoardRole boardRole;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected User() {
        // JPA constructor
    }

    public User(House house, IdentificationDocument identificationDocument, String email, String fullName, ResidentialRole residentialRole) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty.");
        }
        if (identificationDocument == null) {
            throw new IllegalArgumentException("Identification document cannot be null.");
        }
        if (residentialRole == null) {
            throw new IllegalArgumentException("Residential role cannot be null.");
        }
        this.house = house;
        this.identificationDocument = identificationDocument;
        this.email = email.trim().toLowerCase();
        this.fullName = fullName.trim();
        this.residentialRole = residentialRole;
        this.boardRole = BoardRole.NONE;
        this.isActive = true;
        this.createdAt = Instant.now();
    }

    public void assignBoardRole(BoardRole role) {
        if (this.residentialRole != ResidentialRole.OWNER && role != BoardRole.NONE) {
            throw new IllegalStateException("Only an owner assigned to a house can hold a Board of Directors position.");
        }
        this.boardRole = role;
    }

    public UUID getId() {
        return id;
    }

    public House getHouse() {
        return house;
    }

    public IdentificationDocument getIdentificationDocument() {
        return identificationDocument;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public ResidentialRole getResidentialRole() {
        return residentialRole;
    }

    public BoardRole getBoardRole() {
        return boardRole;
    }

    public boolean isActive() {
        return isActive;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
