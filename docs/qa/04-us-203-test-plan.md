# QA Test Plan & Acceptance Matrix: US-203 (Fine Configuration and Application)

This document provides human QA engineers and autonomous AI agents with the exact test specifications, acceptance criteria, and execution instructions for **US-203**.

---

## 1. Scope & Tested User Story
* **US-203:** Fine Configuration Catalog & Fine Application to Houses with Custom or Default Amounts.

---

## 2. Test Execution Protocol for Autonomous AI Agents

To execute this QA test suite autonomously:
1. Ensure the PostgreSQL database container `postgres_local` is running on port 5432 with migration `V4__init_fines_schema.sql` applied.
2. Build and start `finance-service` on port `8082`.
3. Run the automated E2E test script:
   ```bash
   ./tests/e2e/test-us-203.sh
   ```
4. Verify the exit code is `0` and all 9 assertions output `[PASS]`.
5. Validate PostgreSQL state in `finance_schema.fines`.

---

## 3. Test Cases & Acceptance Matrix

| Case ID | Scenario / Objective | Input Payload | Expected HTTP Status | Expected DB / System State |
| :--- | :--- | :--- | :---: | :--- |
| **TC-203-01** | Apply fine using catalog default amount | `houseNumber: "C-101"`, `fineTypeCode: "NOISE_VIOLATION"`, `customAmount: null` | **201 Created** | Fine created in `finance_schema.fines` with default amount Q300.00 and status `PENDING`. |
| **TC-203-02** | Apply fine using custom amount override | `houseNumber: "C-102"`, `fineTypeCode: "SPEEDING"`, `customAmount: 750.00` | **201 Created** | Fine created with overridden amount Q750.00 and status `PENDING`. |
| **TC-203-03** | Invariant: Reject non-existent or inactive fine type | `fineTypeCode: "NON_EXISTENT_VIOLATION"` | **400 Bad Request** | Error code `FINE_TYPE_NOT_FOUND`. No DB changes. |
| **TC-203-04** | Invariant: Reject missing required fields | `houseNumber: ""`, `reason: ""` | **400 Bad Request** | Error title `Validation Error`. No DB changes. |
| **TC-203-05** | Invariant: Reject custom amount <= 0 | `customAmount: -50.00` | **400 Bad Request** | Bean validation fails on `@DecimalMin("0.01")`. |
| **TC-203-06** | Database Integrity: Persistence and foreign keys | Valid request payload | **201 Created** | Record linked to `fine_types` FK and indexed by `house_id` and `house_number`. |

---

## 4. Automated Execution Results Log

```text
=== STEP 0: Finance Service Health Check ===
  [PASS] Finance Service is Healthy (HTTP 200)

=== STEP 1: Testing US-203 (Fine Configuration & Application) ===
  [PASS] 1.1: Apply fine with catalog default amount (NOISE_VIOLATION) (HTTP 201)
  [PASS] Fine 1.1 amount is Q300.00 and status is PENDING
  [PASS] 1.2: Apply fine with custom amount override (Q750.00) (HTTP 201)
  [PASS] Fine 1.2 custom amount verified as Q750.00
  [PASS] 1.3: Invariant - Reject non-existent fine type (400 Bad Request) (HTTP 400)
  [PASS] 1.4: Invariant - Reject payload with missing required fields (400 Bad Request) (HTTP 400)
  [PASS] 1.5: Invariant - Reject non-positive custom amount (400 Bad Request) (HTTP 400)

=== STEP 2: Database State Verification (PostgreSQL) ===
  [PASS] Database verification: 2 fines recorded in finance_schema.fines

========================================================
US-203 E2E Test Summary: 9 Passed, 0 Failed
========================================================
```
