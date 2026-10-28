# QA Test Plan & Acceptance Matrix: US-101 and US-102

This document provides human QA engineers and autonomous AI agents with the exact test specifications, acceptance criteria, and execution instructions for **US-101** and **US-102**.

---

## 1. Scope & Tested User Stories
* **US-101:** House Registration & Owner Onboarding with 48h Invitation Token.
* **US-102:** Account Activation (BCrypt) & Tenant Creation with Single Tenant Invariant.

---

## 2. Test Execution Protocol for Autonomous AI Agents

To execute this QA test suite autonomously:
1. Ensure the PostgreSQL database container `postgres_local` is running on port 5432 (`residencial_db`).
2. Build and start `identity-service` on port `8081`.
3. Run the automated E2E test script:
   ```bash
   ./tests/e2e/test-us-101-and-102.sh
   ```
4. Verify the exit code is `0` and all 9 test assertions output `[PASS]`.
5. Validate database persistence with the included SQL assertions.

---

## 3. Test Cases & Acceptance Matrix

| Case ID | User Story | Scenario / Objective | Input Payload | Expected HTTP Status | Expected DB / System State |
| :--- | :---: | :--- | :--- | :---: | :--- |
| **TC-101-01** | US-101 | Register house with valid Guatemalan CUI (Modulo 11) | `houseNumber: "M-10 L-01"`, `idType: "DPI"`, `idNumber: "1234567820101"` | **201 Created** | House and User created in `identity_schema`. Token generated with 48h expiration. |
| **TC-101-02** | US-101 | Reject duplicate house registration | Existing `houseNumber: "M-10 L-01"` | **400 Bad Request** | Returns error `HOUSE_ALREADY_EXISTS`. No duplicates created. |
| **TC-101-03** | US-101 | Reject invalid CUI checksum (Modulo 11 failure) | `idNumber: "1234567890101"` (invalid verifier) | **400 Bad Request** | Returns `Invalid Argument` with message `"Modulo 11 verification failed"`. |
| **TC-101-04** | US-101 | Register foreign owner with Passport | `idType: "PASSPORT"`, `idNumber: "US99887766"` | **201 Created** | User created with role `OWNER`. |
| **TC-102-01** | US-102 | Activate account with valid invitation token | `token: "<OWNER_TOKEN>"`, `password: "SuperSecurePassword123!"` | **200 OK** | User `is_active` set to `TRUE`, `password_hash` stored in BCrypt format. Token marked `is_used = TRUE`. |
| **TC-102-02** | US-102 | Reject reusing an already consumed invitation token | Re-submit consumed `token` | **400 Bad Request** | Returns error `INVALID_OR_EXPIRED_TOKEN`. |
| **TC-102-03** | US-102 | Owner creates tenant for the house | `houseId`, `idType: "PASSPORT"`, `idNumber: "FR11223344"`, `role: TENANT` | **201 Created** | User created with role `TENANT` linked to the house. Token generated for tenant. |
| **TC-102-04** | US-102 | Invariant: Reject creating a second tenant on same house | Submit second tenant request for same `houseId` | **400 Bad Request** | Returns error `HOUSE_ALREADY_HAS_TENANT`. Database enforces max 1 tenant. |
| **TC-102-05** | US-102 | Direct Database Verification | Query `identity_schema.users` | **SQL Match** | Active owner exists in DB with BCrypt hash and non-null timestamps. |

---

## 4. Verification Checklists for QA Agents
- [ ] Ensure `ddl-auto: validate` is enabled in `application.yml` to guarantee schema fidelity with migrations.
- [ ] Confirm no cleartext passwords appear in database tables or logs.
- [ ] Confirm that `identity_schema.invitation_tokens` has `is_used = TRUE` after activation.
