# QA Test Plan & Acceptance Matrix: US-202 (Bank Reconciliation)

This document provides human QA engineers and autonomous AI agents with the exact test specifications, acceptance criteria, and execution instructions for **US-202**.

---

## 1. Scope & Tested User Story
* **US-202:** Automated Bank Statement Reconciliation (Excel/CSV Upload) for Monthly Maintenance Fees.

---

## 2. Test Execution Protocol for Autonomous AI Agents

To execute this QA test suite autonomously:
1. Ensure the PostgreSQL database container `postgres_local` is running on port 5432 with migration `V3__init_maintenance_and_reconciliation.sql` applied.
2. Build and start `finance-service` on port `8082`.
3. Run the automated E2E test script:
   ```bash
   ./tests/e2e/test-us-202.sh
   ```
4. Verify the exit code is `0` and all 6 assertions output `[PASS]`.
5. Validate PostgreSQL state in `finance_schema.maintenance_charges` and `finance_schema.bank_transactions`.

---

## 3. Test Cases & Acceptance Matrix

| Case ID | Scenario / Objective | Input Payload | Expected HTTP Status | Expected DB / System State |
| :--- | :--- | :--- | :---: | :--- |
| **TC-202-01** | Upload bank report with matching pending charges | CSV/Excel file with `AUTH-GT-88001`, `M-01 L-01`, `650.00` | **200 OK** | Maintenance charge transitioned to `PAID`. Transaction saved with status `RECONCILED`. |
| **TC-202-02** | Handle unmatched house reference without failing the batch | Row with non-existent house reference `M-99 L-99` | **200 OK** | Transaction saved with status `UNMATCHED` and `discrepancy_reason` populated for audit. |
| **TC-202-03** | Invariant & Idempotency: Reject duplicate bank authorizations | Re-uploading file containing already processed authorizations | **200 OK** | Duplicate authorizations detected, counted under `duplicateTransactions`, and skipped without double-crediting. |
| **TC-202-04** | Direct Database Verification | Query `finance_schema.maintenance_charges` | **SQL Match** | Matched charges have `paid_amount = amount` and `status = 'PAID'`. |
