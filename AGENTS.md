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
**Code Style**: Spring Java Format + Spotless + Checkstyle
**Code Quality**: JaCoCo + PMD + SpotBugs + OWASP Dependency Check

## Architecture

The system adopts a **Modular Monolith** architecture with a **Hexagonal Architecture** (Ports & Adapters) approach,
oriented by **Domain-Driven Design (DDD)** for domain modeling and business rules. Development follows **Test-Driven
Development (TDD)** as the standard practice for continuous validation of implemented rules.

**Objective**:
Prevent unintended coupling, centralize business rules in the domain, and reduce inconsistent design decisions
throughout development.

## Documentation & Modeling (Obsidian)

The repository is configured as an **Obsidian Vault** (local settings under `.obsidian/`). The entire requirements gathering lifecycle, feature specifications, and system modeling must be centralized and maintained exclusively under the `docs/` directory.

### Standard Directories
- `docs/requirements/`: Functional requirements, business rules, and acceptance criteria.
- `docs/specs/`: Detailed technical specifications, architecture contracts, and solution design.
- `docs/modeling/`: Conceptual modeling, aggregate maps, and flow diagrams.

### Standards & Conventions
- **WikiLinks:** Use Obsidian internal link syntax (`[[NoteName]]` or `[[path/note|Title]]`) to establish connections across requirements, models, and specifications.
- **Obsidian Canvas (`.canvas`):** Standard adopted for visual domain modeling, flow diagrams, and relationships between modules and entities.
- **Language:** Documentation and notes in Portuguese (as specified in *General Instructions*).
- **Consistency:** Before implementing new features, agents and developers must consult requirements and models in `docs/`, keeping documents and `.canvas` boards updated whenever rules change.

## Project Structure

```txt
stockfy-server
│   .gitattributes
│   .gitignore
│   build.gradle
│   compose.yaml
│   gradlew
│   gradlew.bat
│   HELP.md
│   settings.gradle
├───.obsidian                                   # Obsidian Vault configuration
├───config
│   └───checkstyle
│           checkstyle.xml
├───docs                                        # Documentation and specifications (Obsidian)
│   ├───modeling                                # Visual modeling (.canvas and diagrams)
│   ├───requirements                            # Functional and non-functional requirements
│   ├───specs                                   # Technical specs for epics and stories
│   └───superpowers
│       └───plans                               # Agent execution plans
├───gradle
│   └───wrapper
│           gradle-wrapper.jar
│           gradle-wrapper.properties
├───src
│   ├───main
│   │   ├───generated
│   │   ├───java
│   │   │   └───br
│   │   │       └───com
│   │   │           └───threadstech
│   │   │               └───stockfy             # Root module
│   │   └───resources
│   │       │   application-dev.properties      # Dev profile config
│   │       │   application-prod.properties     # Prod profile config
│   │       │   application.properties          # Common config
│   │       │   messages.properties             # i18n messages
│   │       ├───db
│   │       │   └───migration                   # Flyway migration scripts
│   │       ├───static
│   │       └───templates
│   └───test
│       └───resources
│           │   application.properties          # Test profile config
│           └───sql                             # Test SQL scripts
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
- **Development:** Check specifications and models in `docs/` → write tests first (TDD) → minimal implementation → refactor.
- **Pre-finalization:** Run all tests → validate Checkstyle.
- **Review:** Architecture (DDD + Hexagonal) check; No cross-module coupling; Documentation maintenance.

## Validation & Quality Gates
- Do not modify existing configurations only to make tests or validations pass.
- Do not override configurations to silence warnings, errors, or validation failures.
- Do not suppress warnings without explicit technical justification.
- Fix the root cause instead of bypassing quality checks.

## Agent Context Documentation
To ensure consistency across the project, additional `AGENTS.md` context files are maintained within specific directories:

- `src/main/java/br/com/threadstech/stockfy/modules/AGENTS.md`: Module-level architectural constraints.
- `src/main/java/br/com/threadstech/stockfy/modules/users/AGENTS.md`: Specific context for the User management bounded context.
- `src/main/resources/AGENTS.md`: Global resource-related constraints.
- `src/test/AGENTS.md`: Testing-specific guidelines and conventions.

Always verify the local `AGENTS.md` before performing tasks within these modules.