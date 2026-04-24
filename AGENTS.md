# Project: Stockfy

Stockfy is an ERP/POS system designed for small retail businesses, focused on inventory operations, customer management,
and financial control. This repository contains only the backend, responsible for business logic, persistence, and API
exposure.

## General Instructions

- Do not explain basic concepts unless the user explicitly requests it.
- Always respond in a concise, technical, and direct tone.
- Write the code only in English. Comments and documentation in Portuguese.

## Stack

**Language**: Java 21
**Framework**: Spring Boot 3
**Database**: PostgreSQL
**Integration**: Docker

## Architecture

The system adopts a **Modular Monolith** architecture with a **Hexagonal Architecture** (Ports & Adapters) approach,
oriented by **Domain-Driven Design (DDD)** for domain modeling and business rules. Development follows **Test-Driven
Development (TDD)** as the standard practice for continuous validation of implemented rules.

**Objective**:
Prevent unintended coupling, centralize business rules in the domain, and reduce inconsistent design decisions
throughout development.

## Project Structure

```txt
src/
└── main/
    ├── java/br/com/threadstech/stockfy/
    │   ├── shared/         # Cross-module shared components
    │   ├── modules/        # Bounded Contexts (DDD)
    │   ├── config/         # Global framework configurations
    │   └── StockfyApplication.java
    └── test/               # Mirrors main/java structure
```

## Technical Rules
- **TDD:** Mandatory.
- **Transactional:** Method-level only.
- **Security:** Never include passwords in response DTOs.
- **Git:**
    - Branch: `ddmmaa-feature-name`.
    - Commit: `type: description` (feat, fix, refactor, test, chore).
    - Do not execute git commands not documented here.

## Coding Style
- Prefer `Optional` over `null`.
- DTOs: `record` by default.
- Indentation: Google Checkstyle.
- Imports: **Never** use star imports (`import ...*`).
- Naming: 
    - No class names with 2+ consecutive uppercase letters (e.g., `XMLParser` is forbidden; use `XmlParser`).
    - Use clear domain nouns (e.g., `Order`, `Customer`).
- Lombok: Use `@Getter`, `@Setter`, `@Builder`, `@AllArgsConstructor` / `@RequiredArgsConstructor`.
- Avoid: `Manager`, `Handler`, `Utils`, `Service` (without context).
- Naming (Methods/DTOs): Verb + Entity + `UseCase` (Use Cases); Past tense + `Event` (Events); `Request`/`Response` (DTOs).

## Workflow
- **Development:** Write tests first → minimal implementation → refactor.
- **Pre-finalization:** Run all tests → validate Checkstyle.
- **Review:** Architecture (DDD + Hexagonal) check; No cross-module coupling.

### Agent Work Log (Mandatory)
- Every agent must create and maintain a documentation file during task execution.
- File location: `/docs/tasks/<TASK>.md`
- This file must contain:
    - What was attempted.
    - Why it was attempted.
    - Result (success or failure).
    - Observations and possible improvements.
- All actions must be recorded continuously during execution.
- Objective:
    - Build a knowledge base of what works and what does not.
    - Improve future decision-making.
    - Provide traceability for analysis and optimization of agent behavior.