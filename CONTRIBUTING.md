# Contributing Guidelines

Welcome to the Residential Management System project. All team members (human engineers and AI agents) follow these engineering workflows.

---

## 1. Getting Started
- Read the agent & developer guidelines in [AGENTS.md](AGENTS.md).
- Review the project documentation inside the [docs/](docs/) folder:
  - Architecture Design: [docs/01-architecture-design.md](docs/01-architecture-design.md)
  - Tech Stack Analysis: [docs/02-technology-stack-analysis.md](docs/02-technology-stack-analysis.md)
  - User Stories Backlog: [docs/03-user-stories-backlog.md](docs/03-user-stories-backlog.md)
  - Requirements TRD: [docs/requirements/](docs/requirements/)

---

## 2. Git & Branching Strategy
- Branch off `develop`:
  ```bash
  git checkout develop
  git pull origin develop
  git checkout -b feature/<task-id>-<description>
  ```
- Before creating a PR or completing a task, sync with `develop`:
  ```bash
  git fetch origin && git merge origin/develop
  ```
- Commit using Conventional Commits:
  ```bash
  git commit -m "feat(identity): add house registration use case"
  ```

---

## 3. Testing Standard
- Execute the test suite before committing:
  ```bash
  cd services/identity-service && mvn test
  ```
- 100% pass rate is strictly required.

---

## 4. Documentation
- All project documentation, ADRs, and guides must be placed inside the `docs/` folder in this repository.
