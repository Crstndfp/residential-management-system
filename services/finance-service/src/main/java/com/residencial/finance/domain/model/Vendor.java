package com.residencial.finance.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vendors", schema = "finance_schema")
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tax_id", nullable = false, unique = true, length = 30)
    private String taxId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Vendor() {
        // JPA constructor
    }

    public Vendor(String taxId, String name, String contactEmail, String contactPhone) {
        if (taxId == null || taxId.trim().isEmpty()) {
            throw new IllegalArgumentException("Tax ID (NIT/RFC) cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Vendor name cannot be empty.");
        }
        this.id = UUID.randomUUID();
        this.taxId = taxId.trim().toUpperCase();
        this.name = name.trim();
        this.contactEmail = contactEmail != null ? contactEmail.trim().toLowerCase() : null;
        this.contactPhone = contactPhone != null ? contactPhone.trim() : null;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getTaxId() { return taxId; }
    public String getName() { return name; }
    public String getContactEmail() { return contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public Instant getCreatedAt() { return createdAt; }
}
