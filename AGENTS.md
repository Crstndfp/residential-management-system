# Residential Management System (Sistema de Gestión Residencial) — Team & Agent Guidelines

This document defines the strict engineering standards, architectural patterns, workflow protocols, and coding rules that all developers and AI agents MUST adhere to when working on this repository.

---

## 1. General Principles & Language
- **Strict English Codebase:** All code, domain models, entity definitions, variables, database migrations, API contracts, code comments, tests, and commit messages MUST be written in **English**.
- **In-Project Documentation:** All architecture documents, requirements, ADRs, technical guides, and specifications MUST reside directly inside this repository under the `docs/` folder. Do not store project docs outside the repository.
- **SOLID & Clean Architecture:** Strictly follow SOLID principles. Favor small, cohesive interfaces and single-responsibility handlers. Decouple domain logic from frameworks.
- **Domain-Driven Design (DDD):** Encapsulate business invariants inside domain entities/aggregates. Avoid anemic models with unrestricted public setters; enforce state transitions through explicit domain methods (e.g., `user.assignBoardRole(role)`).

---

## 2. Multi-Agent & Git Concurrency Safety
- **Check Branch First:** Never assume the current branch. Always verify with `git branch --show-current` before making changes.
- **Branch Naming Standard:**
  - Feature: `feature/<task-id>-<short-description>`
  - Bugfix: `fix/<task-id>-<short-description>`
  - Always branch off `develop` (never directly from `main`/`master`).
- **Single Branch Per Task/Conversation:** Keep all related work on the task's assigned branch. Do not create multiple branches for the same conversation.
- **Concurrency & Local Repo Protection:** Multiple developers or AI agents may operate in this repository simultaneously.
  - Run `git status` before touching files or checking out branches.
  - Never run destructive commands (`git reset --hard`, `git clean -fd`, `git checkout -- .`) without explicit review.
  - Keep your branch synchronized with `develop`: `git fetch origin && git merge origin/develop` (or local equivalent).
- **Atomic Conventional Commits:**
  - Format: `<type>(<scope>): <concise description in imperative mood>`
  - Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`.
  - Example: `feat(identity): enforce single owner invariant per house`

---

## 3. Architecture & Microservices Standards
- **Microservices Boundaries (Bounded Contexts):**
  1. `identity-service` (port 8081): Users, houses, board roles, onboarding.
  2. `finance-service` (port 8082): Invoices, maintenance fees, bank reconciliation, fines, assets.
  3. `amenities-service` (port 8083): Common areas, bookings, assemblies, voting, maintenance tickets.
  4. `parking-service` (port 8084): Visitor registration, stay tracking, overnight parking heuristic.
- **Database Schema Isolation (PostgreSQL 16):**
  - Each microservice operates within its designated PostgreSQL schema: `identity_schema`, `finance_schema`, `amenities_schema`, `parking_schema`.
  - Services must NOT perform cross-schema joins. Inter-service communication happens via REST APIs or RabbitMQ asynchronous events.
- **Single-Tenant to Multi-Tenant Readiness:**
  - Do NOT hardcode `tenant_id` query filters at this stage.
  - ALL primary and foreign keys MUST use `UUID` (UUIDv7 or UUIDv4) to prevent ID collisions when transitioning to multi-tenancy in the future.
- **Storage & Column Optimization:**
  - Avoid unbounded `TEXT` for structured fields; use bounded `VARCHAR(n)` (e.g., `VARCHAR(15)` for license plates, `VARCHAR(20)` for house numbers).
  - Use `NUMERIC(12, 2)` for monetary values (never float/double).

---

## 4. Autonomous Execution, Quality & Testing Gate
- **Autonomous Execution:** Agents have full autonomy to create, edit, and delete files inside this project without blocking for code approval.
- **Architectural Rationale First (What & Why):** Before executing a task or User Story, briefly explain **what** components/files will be worked on and the architectural/business **why** behind those decisions. Do not block asking to review raw code snippets unless explicitly requested by the user.
- **Mandatory Unit Tests:**
  - Every domain model, entity invariant, application use-case, and validator MUST have unit tests.
  - Run test suites before committing (`mvn test` / `./mvnw test`).
  - Never finish a task or push with failing tests.
- **Error Handling & Localization:**
  - Domain exceptions must carry explicit error codes and meaningful messages.
  - Return standardized REST error responses (RFC 7807 Problem Details).
