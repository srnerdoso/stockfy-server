# Module and Backend Standards

## Directory Structure (Example)
Each module must follow the standard layered structure:
```txt
modules/example-module/
├── application/
│   ├── usecase/    # Flow orchestration
│   ├── dto/        # Request/Response records
│   └── port/       # Interfaces for external dependencies
├── domain/
│   ├── model/      # Aggregates, Entities, Value Objects
│   ├── service/    # Domain Services
│   └── event/      # Domain Events
├── infrastructure/
│   ├── persistence/ # JPA entities, Repository impl
│   ├── messaging/   # RabbitMQ/Kafka publishers/consumers
│   └── config/      # Module-specific config
└── presentation/
    └── controller/  # REST controllers (thin)
```

## Backend Structure

### Layers

**Domain**
- Rich entities (non-anemic)
- Value Objects
- Aggregates and Aggregate Roots
- Pure business rules
- Domain Services
- Domain Events

**Application**
- Use Cases / Application Services
- Flow orchestration
- Coordination between domains via interfaces
- Input validation
- Event publishing

**Infrastructure**
- Persistence implementations (JPA, SQL)
- External adapter implementations
- Port implementations
- Framework configurations

**Inbound Adapters (Presentation Layer)**
- REST Controllers
- Event consumers
- Jobs / schedulers
- DTO → Application Command conversion

### Business Rules
- Centralized in the domain (entities, value objects, domain services).
- Entities must enforce internal consistency (invariants).
- Aggregates control transactional consistency.
- **Prohibited**: Business rules in controllers, application services, or infrastructure layers.

### APIs
- REST as the primary standard.
- Thin controllers.
- DTOs strictly separated from the domain.
- **Versioning:** Mandatory for all endpoints (e.g., `/api/v1/...`).

## Implementation Rules
- **Domain First:** Always start from the domain layer. Entities must enforce invariants.
- **Dependency Direction:** Dependencies always point toward the domain.
- **Events:** Use selectively for inter-module side effects. Avoid using for simple synchronous flows.
- **Consistency:** Maintain aggregate transactional boundaries.

## Inter-Module Communication
- **Isolation:** Exclusively via interfaces (ports).
- **Forbidden:** Never direct access to entities, repositories, or services of another module.
- **Integration:** Use Application Services as contracts or Domain Events for asynchronous state changes.
- **Impact:** Always review cross-module impact before changes.

## Caching
- Do not cache domain entities or JPA entities.
- Do not cache objects tied to persistence context (lazy-loaded, proxies).
- Cache only data used for read operations.

## Exception Handling
- **Forbidden:** Usage of generic exceptions (e.g., IllegalArgumentException, RuntimeException, etc.) for business errors.
- Always use **specific custom exceptions** that represent the business context.
    - Example: `InvalidPasswordException` instead of `IllegalArgumentException`.
- Custom exceptions must be **explicit and meaningful**, reflecting the exact failure.