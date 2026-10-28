# QA Test Plan & Acceptance Matrix: US-204 (Personal Account Statement)

This document provides human QA engineers and autonomous AI agents with the exact test specifications, acceptance criteria, and execution instructions for **US-204**.

---

## 1. Scope & Tested User Story
* **US-204:** Personal Account Statement Query (`/api/v1/finance/account-statements/{houseId}`) with Solvency Evaluation and Tenant Access Restriction.

---

## 2. Test Execution Protocol for Autonomous AI Agents

To execute this QA test suite autonomously:
1. Ensure the PostgreSQL database container `postgres_local` is running on port 5432 with migrations `V1` through `V4` applied.
2. Build and start `finance-service` on port `8082`.
3. Run the automated E2E test script:
   ```bash
   ./tests/e2e/test-us-204.sh
   ```
4. Verify the exit code is `0` and all 8 assertions output `[PASS]`.
5. Validate response structure, solvency status evaluation, and tenant restriction.

---

## 3. Test Cases & Acceptance Matrix

| Case ID | Scenario / Objective | Input Payload / Headers | Expected HTTP Status | Expected DB / System State |
| :--- | :--- | :--- | :---: | :--- |
| **TC-204-01** | Retrieve account statement as OWNER for solvent house | `X-User-Role: OWNER`, `houseId: M-01 L-01` | **200 OK** | Evaluated as `SOLVENT` with `totalBalance: 0`, and chronological list of charges and reconciled payments. |
| **TC-204-02** | Retrieve account statement for house with pending fine | `X-User-Role: OWNER`, `houseId: C-101` | **200 OK** | Evaluated as `DELINQUENT` with `totalBalance: 300.00` and `pendingFinesBalance: 300.00`. |
| **TC-204-03** | Invariant: Reject access to TENANT user | `X-User-Role: TENANT` | **403 Forbidden** | Error code `TENANT_STATEMENT_ACCESS_DENIED`. Account statement data is not disclosed. |
| **TC-204-04** | Invariant: Reject malformed UUID path variable | `houseId: invalid-uuid-format` | **400 Bad Request** | Request rejected before domain execution. |
| **TC-204-05** | Chronological ordering of movements | Valid request | **200 OK** | Movements array sorted by `date` descending. |

---

## 4. Automated Execution Results Log

```text
=== STEP 0: Finance Service Health Check ===
  [PASS] Finance Service is Healthy (HTTP 200)

=== STEP 1: Testing US-204 (Personal Account Statement) ===
  [PASS] 1.1: Retrieve account statement for OWNER (HTTP 200) (HTTP 200)
  [PASS] House M-01 L-01 correctly evaluated as SOLVENT with Q0.00 balance
  [PASS] 1.2: Retrieve statement for house with pending fine (HTTP 200) (HTTP 200)
  [PASS] House C-101 correctly evaluated as DELINQUENT with Q300.00 fine balance
  [PASS] 1.3: Invariant - Block TENANT role with 403 Forbidden (HTTP 403)
  [PASS] Tenant rejection verified with errorCode: TENANT_STATEMENT_ACCESS_DENIED
  [PASS] 1.4: Invariant - Reject malformed UUID path variable (HTTP 400)

========================================================
US-204 E2E Test Summary: 8 Passed, 0 Failed
========================================================
```
