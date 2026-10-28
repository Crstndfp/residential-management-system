# QA Test Plan & Acceptance Matrix: US-201 (Expense Invoices)

This document provides human QA engineers and autonomous AI agents with the exact test specifications, acceptance criteria, and execution instructions for **US-201**.

---

## 1. Scope & Tested User Story
* **US-201:** Register Expense Invoices with Vendor Resolution and Categorization.

---

## 2. Test Execution Protocol for Autonomous AI Agents

To execute this QA test suite autonomously:
1. Ensure the PostgreSQL database container `postgres_local` is running on port 5432 with migration `V2__init_finance_schema.sql` applied.
2. Build and start `finance-service` on port `8082`.
3. Run the automated E2E test script:
   ```bash
   ./tests/e2e/test-us-201.sh
   ```
4. Verify the exit code is `0` and all 7 assertions output `[PASS]`.
5. Validate PostgreSQL state in `finance_schema.invoices`.

---

## 3. Test Cases & Acceptance Matrix

| Case ID | Scenario / Objective | Input Payload | Expected HTTP Status | Expected DB / System State |
| :--- | :--- | :--- | :---: | :--- |
| **TC-201-01** | Register invoice with auto-created vendor and active category | `invoiceNumber: "FAC-2026-901"`, `vendorTaxId: "NIT-998877-K"`, `categoryCode: "SECURITY"`, `amount: 14500.50` | **201 Created** | Vendor and Invoice persisted in `finance_schema`. Status set to `PENDING`. |
| **TC-201-02** | Invariant: Reject duplicate invoice number for same vendor | Same `invoiceNumber` and `vendorTaxId` as TC-201-01 | **400 Bad Request** | Returns `DUPLICATE_INVOICE`. Prevents duplicate billing. |
| **TC-201-03** | Reject invoice with non-existent category code | `categoryCode: "NON_EXISTING_CAT"` | **400 Bad Request** | Returns `CATEGORY_NOT_FOUND`. |
| **TC-201-04** | Invariant: Reject invoice where due date is before issue date | `issueDate: "2026-10-25"`, `dueDate: "2026-10-10"` | **400 Bad Request** | Returns `Invalid Argument` (`Due date cannot be before issue date`). |
| **TC-201-05** | Register second invoice for distinct vendor & category | `invoiceNumber: "ELEC-2026-OCT"`, `categoryCode: "ELECTRICITY"`, `amount: 4820.75` | **201 Created** | Invoice created under `ELECTRICITY` category. |
| **TC-201-06** | Direct Database Verification | Query `finance_schema.invoices` | **SQL Match** | Exactly 2 invoices persisted with `NUMERIC(12,2)` precision and foreign key integrity. |
