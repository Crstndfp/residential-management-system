package com.residencial.finance.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "invoices", schema = "finance_schema")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "invoice_number", nullable = false, length = 50)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ExpenseCategory category;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(name = "document_url", length = 255)
    private String documentUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Invoice() {
        // JPA constructor
    }

    public Invoice(String invoiceNumber, Vendor vendor, ExpenseCategory category,
                   LocalDate issueDate, LocalDate dueDate, BigDecimal amount,
                   String documentUrl, String notes) {
        if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Invoice number cannot be empty.");
        }
        if (vendor == null) {
            throw new IllegalArgumentException("Vendor cannot be null.");
        }
        if (category == null) {
            throw new IllegalArgumentException("Expense category cannot be null.");
        }
        if (issueDate == null) {
            throw new IllegalArgumentException("Issue date cannot be null.");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date cannot be null.");
        }
        if (dueDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("Due date cannot be before issue date.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invoice amount must be strictly greater than zero.");
        }

        this.id = UUID.randomUUID();
        this.invoiceNumber = invoiceNumber.trim();
        this.vendor = vendor;
        this.category = category;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.paymentDate = null;
        this.amount = amount;
        this.status = InvoiceStatus.PENDING;
        this.documentUrl = documentUrl != null ? documentUrl.trim() : null;
        this.notes = notes != null ? notes.trim() : null;
        this.createdAt = Instant.now();
    }

    public void markAsPaid(LocalDate paymentDate) {
        if (this.status == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Cannot pay a cancelled invoice.");
        }
        if (paymentDate == null) {
            throw new IllegalArgumentException("Payment date cannot be null.");
        }
        this.paymentDate = paymentDate;
        this.status = InvoiceStatus.PAID;
    }

    public void cancel(String reason) {
        if (this.status == InvoiceStatus.PAID) {
            throw new IllegalStateException("Cannot cancel an invoice that has already been paid.");
        }
        this.status = InvoiceStatus.CANCELLED;
        if (reason != null && !reason.trim().isEmpty()) {
            this.notes = (this.notes == null ? "" : this.notes + "\nCancellation reason: ") + reason.trim();
        }
    }

    public UUID getId() { return id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public Vendor getVendor() { return vendor; }
    public ExpenseCategory getCategory() { return category; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public BigDecimal getAmount() { return amount; }
    public InvoiceStatus getStatus() { return status; }
    public String getDocumentUrl() { return documentUrl; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
}
