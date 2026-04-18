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

**Main Libraries**:

- MapStruct
- Spring Security
- Hibernate Validation
- Hibernate Envers
- Flyway
- JJWT
- Lombok
- OpenAPI
- Spring Modulith

## Architecture

The system adopts a **Modular Monolith** architecture with a **Hexagonal Architecture** (Ports & Adapters) approach,
oriented by **Domain-Driven Design (DDD)** for domain modeling and business rules. Development follows **Test-Driven
Development (TDD)** as the standard practice for continuous validation of implemented rules.

Communication between modules occurs exclusively through explicit interfaces and contracts, ensuring low coupling and
context isolation.

The architecture is complemented by **Event-Driven Architecture (EDA)** only in specific domain-defined scenarios (e.g.,
low stock events, completed sales), avoiding indiscriminate use of events.

Architectural structure includes:

- Base pattern: Modular Monolith + Hexagonal Architecture
- Organization by domains (DDD Bounded Contexts)
- Layer separation: Domain, Application, and Infrastructure
- Internal communication via interfaces and ports (Ports & Adapters)
- Controlled inbound flow: API → Application → Domain → Persistence

**Objective**:
Prevent unintended coupling, centralize business rules in the domain, and reduce inconsistent design decisions
throughout development.

## Backend

Structure is organized by domain modules (DDD), isolating contexts and rules.

### Layers

**Domain**

- Rich entities (non-anemic)
- Value Objects
- Aggregates and Aggregate Roots
- Pure business rules
- Domain Services (when the rule does not belong to a single entity)
- Domain Events (when applicable)

**Application**

- Use Cases / Application Services
- Flow orchestration (without complex business logic)
- Coordination between domains via interfaces
- Input validation (not related to domain rules)
- Event publishing (when required)

**Infrastructure**

- Persistence implementations (JPA, SQL, etc.)
- External adapter implementations (messaging, external APIs)
- Port implementations (repositories, gateways)
- Framework configurations (Spring, etc.)

**Inbound Adapters (Presentation Layer)**

- REST Controllers
- Event consumers
- Jobs / schedulers
- DTO → Application Command conversion

### Business Rules

- Centralized in the domain (entities, value objects, and domain services)
- Entities must enforce internal consistency (invariants)
- Aggregates control transactional consistency
- **Prohibited**: Business rules in controllers, application services, or infrastructure layers
- Shared rules between modules must be exposed via domain interfaces or explicit contracts
- Domain events are used only for relevant side effects (e.g., inventory, billing)
- Input validations must not replace domain rules

### APIs (System Entry Point)

**Responsibility**

- Expose module use cases
- Translate external requests into application commands
- Contain no business logic

**Format**

- REST as the primary standard
- Thin controllers
- DTOs strictly separated from the domain
- API versioning when necessary (`/api/v1/...`)

**Standard Flow**

- Request → Controller → Use Case → Domain → Repository (port) → Infrastructure

**Contracts**

- Input DTOs (Request DTO)
- Output DTOs (Response DTO)
- Interfaces (ports) for external dependencies
- Domain contract for published events (decoupled from infrastructure)

### Inter-Module Communication

- Exclusively via interfaces (ports)
- Never direct access to entities from another module
- Preferred integration methods:
    - Application Services exposed as contracts
    - Domain Events (when explicitly required)

- Dependencies always point toward the domain, never the opposite direction.

### Events (Selective EDA)

Used only for:

- Relevant state changes across modules
- Asynchronous processes (e.g., inventory, billing, auditing)

- Publication occurs in the Application or Domain layer (depending on the case)
- Consumption is handled as an inbound adapter (infrastructure)
- Not required for simple synchronous flows

## Technical Rules

- Automated tests must be written strictly before any code change. See: [Testing](#testing)
- Do not annotate classes with `@Transactional`. Apply it only at the method level.
- Never include passwords in response DTOs or return any password field in any client response.
- When implementing a new feature, always create a new branch named in the format `ddmmaa-feature-name` and switch to
  it.
- Do not execute any git commands that are not explicitly documented here or requested by the user.
- Do not modify `.gitignore` without explicit user request.

## Key Dependencies

- Spring Security
- Spring Data JPA
- PostgreSQL Driver
- Flyway
- JJWT
- RabbitMQ
- Redis

## Project Structure

```txt
src/
└── main/
    ├── java/br/com/threadstech/stockfy/
    │
    │   ├── shared/
    │   │   ├── domain/
    │   │   ├── application/
    │   │   └── infrastructure/
    │   ├── modules/
    │   │   ├── example1/
    │   │   │   ├── domain/
    │   │   │   │   ├── model/
    │   │   │   │   ├── service/
    │   │   │   │   ├── event/
    │   │   │   │   └── repository/
    │   │   │   ├── application/
    │   │   │   │   ├── usecase/
    │   │   │   │   ├── dto/
    │   │   │   │   └── port/
    │   │   │   ├── infrastructure/
    │   │   │   │   ├── persistence/
    │   │   │   │   ├── messaging/
    │   │   │   │   ├── config/
    │   │   │   │   └── external/
    │   │   │   └── presentation/
    │   │   │       └── controller/
    │   │   ├── example2/
    │   │   └── example3/
    │   └── app/
    │       └── StockfyApplication.java
    └── test/
        └── java/br/com/threadstech/stockfy/
            ├── modules/
            └── shared/
```

## Coding Style

- Prefer `Optional` over `null`.
- DTOs should be implemented as `record` by default. Use `class` only when mutability, no-arg constructor, inheritance,
  or additional logic beyond data transport is required.
- Indentation follows Google Checkstyle standards.
- Never manually create getters or setters.
- Classes: Use clear domain nouns (e.g., `Order`, `Customer`).
- Methods: Use verbs that express action (e.g., `createOrder`, `calculateTotal`).
- Variables: Use descriptive and specific names (e.g., `totalAmount`, `customerName`).
- Booleans: Prefix with `is`, `has`, `can` (e.g., `isActive`, `hasStock`).
- Interfaces: Named by role/contract (e.g., `PaymentGateway`, `OrderRepository`).
- Implementations: Include technical detail when relevant (e.g., `JpaOrderRepository`, `StripePaymentGateway`).
- Use Cases: Verb + Entity + `UseCase` suffix (e.g., `CreateOrderUseCase`).
- Events: Past tense + `Event` suffix (e.g., `OrderCreatedEvent`).
- DTOs: Use `Request` and `Response` suffixes (e.g., `CreateOrderRequest`, `OrderResponse`).
- Avoid generic names such as `Manager`, `Handler`, `Utils`, or `Service` without specific context.
- Use Lombok by default to reduce boilerplate (`@Getter`, `@Setter`, `@Builder`, `@AllArgsConstructor`, etc.).
- Avoid manual writing of getters, setters, constructors, or builders when Lombok can replace them.
- Use `@RequiredArgsConstructor` for dependency injection.
- Avoid excessive unnecessary annotations; keep only those essential for clarity and maintainability.

## Commands

- Run application (dev):
  ./gradlew bootRun

- Run application with specific profile:
  ./gradlew bootRun --args='--spring.profiles.active=dev'

- Build project:
  ./gradlew clean build

- Build without tests:
  ./gradlew clean build -x test

- Run tests:
  ./gradlew test

- Run a specific test:
  ./gradlew test --tests TestClassName

- Run more than one specific test:
  ./gradlew test --tests TestClassName --tests TestClassName2

- Run tests by method:
  ./gradlew test --tests TestClassName.methodName

- Start infrastructure (Docker):
  docker compose up -d

- Stop infrastructure:
  docker compose down

- View Docker logs:
  docker compose logs -f

- Run migrations (Flyway via app):
  automatic when starting the application

- Generate OpenAPI documentation:
  ./gradlew generateOpenApiDocs

- Validate style (Checkstyle):
  ./gradlew checkstyleMain checkstyleTest

- Generate package (JAR):
  ./gradlew bootJar

- Run JAR:
  java -jar build/libs/*.jar

- Clean build:
  ./gradlew clean

- Create and switch branch:
  git checkout -b branch-name

## Testing

- Framework: JUnit 5 + Mockito + Spring Boot Test + Testcontainers.

- Approach: **TDD mandatory** (write tests before implementation).

- Coverage by layer:
    - **Domain**: Pure unit tests (no Spring, no heavy mocks).
    - **Application**: Unit tests with mocks (Mockito).
    - **Infrastructure**: Integration tests (Testcontainers + real PostgreSQL).
    - **Presentation**: API tests (MockMvc or WebTestClient).

- Rules:
    - Every new behavior must have a test.
    - Every business rule must be covered by domain tests.
    - Do not test getters/setters or trivial code.
    - Tests must be independent and deterministic.
    - Do not share state between tests.
    - Test names must describe the expected behavior.
    - Tests are immutable by default:
        - Do not modify existing tests.
        - New behaviors require new tests.
    - Exception: Tests may be changed only if they are incorrect, inconsistent, or obsolete with the domain, **or** if
      the behavior change was intentional and explicit.

- Security Rule:
    - Never modify tests just to make code pass.
    - If a test fails, first assume the code is wrong.

- Priority:
    - Tests define the expected behavior (source of truth).

- Naming Pattern:
  `ClassNameTest`  
  `method_whenCondition_thenExpectedResult`

- Structure:
  `src/test/java/...` mirrors `src/main/java/...` structure.

- Mocks:
    - Use only in the Application layer.
    - Never mock the domain.
    - Prefer real objects whenever possible.

- Integration:
    - Use Testcontainers for PostgreSQL.
    - Do not use in-memory databases (e.g., H2).

- Events:
    - Test publishing and consumption separately.
    - Validate side effects, not internal implementation.

- Performance:
    - Unit tests must be fast.
    - Integration tests must be isolated and controlled.

- Test Data:
    - Use factories/builders for test objects.
    - Avoid duplicated setup.

- Failures:
    - Tests must cover all error and exception scenarios.

- Rate Limit:
    - Test allowed request limit within the configured window.
    - Test blocking when limit is exceeded (HTTP 429).
    - Test limit reset after window expiration.
    - Validate behavior per identity (IP, user, or token).
    - Ensure different identities do not share limits.

## Workflow

- Standard Flow:
    - Memorize current branch before starting (base branch)
    - Get current date via terminal
    - Create branch: `ddmmaa-feature-name`
    - Always derive from the current branch (not necessarily `main`)

- Development:
    - Write tests first (TDD)
    - Implement the minimum to make tests pass
    - Refactor while keeping tests green
    - Small, incremental commits

- Commits:
    - Short and objective messages
    - Format: `type: description`
    - Types: `feat`, `fix`, `refactor`, `test`, `chore`
    - Example: `feat: create order use case`

- Pre-finalization Validation:
    - Run all tests
    - Ensure successful build
    - Validate Checkstyle
    - Start application locally if necessary

- Integration (Pull Request):
    - PR must target the origin branch (memorized base branch)
    - Never assume `main` as default
    - Example:
        - Current branch: `dev-nerdoso`
        - New branch: `17042026-products`
        - PR → `dev-nerdoso`

- Review:
    - Code must respect architecture (DDD + Hexagonal)
    - No business rules outside the domain
    - No coupling between modules

## Do

**Recommended Patterns**:

- Centralize business rules in the domain (entities, VOs, domain services)
- Enforce invariants inside entities
- Model aggregates with clear boundaries and transactional consistency
- Use Use Cases exclusively for flow orchestration
- Communicate between modules only via interfaces (ports)
- Completely isolate domain from infrastructure
- Use Ports & Adapters for any external dependency
- Apply TDD (tests before implementation)
- Cover business rules with domain tests
- Keep tests deterministic, independent, and fast
- Use Testcontainers for PostgreSQL integration tests
- Keep controllers thin (input/output only)
- Use DTOs for external communication (separated from domain)
- Perform input validation in the Application layer
- Use `record` for DTOs whenever possible
- Name classes, methods, and variables explicitly and unambiguously
- Follow defined naming conventions (UseCase, Event, Request/Response, etc.)
- Maintain low coupling between modules and high cohesion within modules
- Prefer composition over inheritance
- Use MapStruct for mapping between layers
- Use Lombok to reduce boilerplate
- Apply `@Transactional` only at method level
- Use declarative validations (Hibernate Validation) for input only
- Use domain events only for relevant side effects
- Ensure idempotency in critical operations when required
- Version APIs when contract-breaking changes occur
- Keep commits small, incremental, and semantic

**Expected Behaviors**:

- Always start implementation by writing tests (TDD)
- Always check current branch before creating a new one
- Always derive new branches from the current branch
- Always use branch name format `ddmmaa-feature-name`
- Always run tests before finalizing any change
- Always ensure a green build before opening a PR
- Always validate Checkstyle before finalizing
- Always open PR against the origin branch (base branch)
- Always keep commits small and focused
- Always follow project naming conventions
- Always start implementation from the domain layer
- Always ensure business rules remain in the domain
- Always use Use Cases only for orchestration
- Always access external dependencies via interfaces (ports)
- Always maintain module isolation
- Always validate inputs in the Application layer
- Always map DTO ↔ Domain using mappers (MapStruct)
- Always ensure controllers contain no business logic
- Always write tests covering success and error scenarios
- Always handle errors explicitly
- Always maintain aggregate consistency
- Always review impact on other modules before integration
- Always keep code aligned with defined architecture
- Always prioritize clarity over unnecessary abstraction
- Always remove dead or unused code
- Always maintain package organization standards

## Don't

**Common Errors**:

- Placing business rules in controllers, use cases, or infrastructure
- Creating anemic entities (without behavior)
- Directly accessing entities or repositories from another module
- Coupling the domain with frameworks (Spring, JPA, etc.)
- Using DTOs as domain entities or vice versa
- Exposing domain entities directly in APIs
- Ignoring domain invariants
- Breaking aggregate boundaries
- Using events without real need
- Using events for simple synchronous flows
- Creating generic services without context (`UserService`, `Manager`, etc.)
- Creating utility classes (`Utils`) for business logic
- Duplicating logic across modules
- Creating circular dependencies between modules
- Using `null` instead of `Optional`
- Creating overly generic methods or classes
- Mixing input validation with domain rules
- Placing logic in mappers
- Using `@Transactional` at class level
- Ignoring test failures
- Modifying tests to make code pass
- Not covering error scenarios in tests
- Sharing state between tests
- Using in-memory databases instead of Testcontainers
- Not validating inter-module impact
- Ignoring project naming conventions
- Creating large commits with multiple responsibilities
- Opening PR to the wrong branch (e.g., assuming `main`)
- Executing undocumented git commands
- Returning sensitive data in APIs

**Prohibited Practices**:

- Placing business rules outside the domain
- Directly accessing entities from another module
- Directly accessing repositories from another module
- Coupling domain with frameworks or infrastructure
- Using DTO as domain entity
- Exposing entities directly in APIs
- Creating anemic entities
- Breaking domain invariants
- Violating aggregate boundaries
- Creating circular dependencies between modules
- Duplicating business rules across modules
- Using events without domain justification
- Using events to replace simple synchronous flows
- Creating generic classes without context (`Service`, `Manager`, `Handler`, `Utils`)
- Placing business logic in mappers
- Using `null` when `Optional` is available
- Using `@Transactional` at class level
- Modifying tests to make code pass
- Ignoring test or build failures
- Using in-memory databases for integration tests
- Returning sensitive data (e.g., passwords)
- Executing undocumented git commands
- Modifying `.gitignore` without authorization
- Creating branches outside the defined pattern
- Opening PR to the incorrect branch
- Implementing without prior tests (TDD violation)