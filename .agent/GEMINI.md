# Project: Stockfy

## DEPRECATED ARCHITECTURE NOTICE

This document describes a legacy layered, single-module architecture.
Sections marked with [LEGACY] do not apply to the modular architecture.
New features must follow the modular architecture guidelines.

### Migration Instructions

- The user must explicitly specify the target module. **Restricted permission:** read, create, update, and delete
  operations are allowed **only within the specified module**.

- **Test-first is mandatory:**

    - Create 100% of automated tests before any implementation;
    - Tests must not be modified after creation.

- **Continuous test execution:**

    - Run the module's tests after each implementation;
    - Fix any failures and re-run the tests until all tests pass.

- **Legacy code:** read-only access only.

- **Database:**

    - Use Flyway migrations;
    - Direct creation or modification of tables in entities is prohibited.

- **Scope restrictions:**

    - Do not implement anything marked as `[LEGACY]`.

- **Module quality:**

    - The module must be delivered fully functional and compliant with business rules.

- **Versioning and endpoints:**

    - Use the `/api/v1_1` prefix for new modules;
    - Define endpoints in `ApiPaths.java` following the project's naming conventions;
    - Use the `V1_1` suffix for versioning.

- Use rate limiting;

## [LEGACY] Architecture

- Layered Architecture
- Single module application
- Package-by-layer
- Controllers in `/web/controller`
- Services in `/service`
- Repositories in `/repository`
- Entities in `/entity`
- Packages can access services from other features (e.g. purchase features may access user services)

There is **no separation** between domain, application, and infrastructure layers.

## Architecture

- Single application with multiple isolated feature modules.
- Package-by-feature organization.
- Each module contains:

    - Controllers
    - Services (simple business rules)
    - Use Cases (complex or cross-module rules; prefer switch/case over if)
    - Repositories
    - Entities / Domain models

- Separate directories (service, entity, repository, controller) only if multiple classes of the same type exist (e.g.,
  DTOs, Use Cases) to avoid clutter.
- Modules are self-contained and independent.
- Direct module access is restricted; communication via events only.

### Architecture Traits

- Clear separation of domain, application, infrastructure layers within modules.
- High cohesion within modules, low coupling between modules.
- Shared code minimized; placed in core/shared modules.
- Improves maintainability, scalability, testability without distributed system complexity.

## [LEGACY] Directory Structure

Current project structure:

```
br/com/threadstech/stockfy
├─ api
├─ components
├─ config
│  ├─ constraints
│  └─ properties
├─ entity
│  └─ base
├─ enums
├─ exception
├─ repository
│  └─ spec
├─ security
│  ├─ jwt
│  └─ refreshtoken
├─ service
│  └─ scheduler
├─ utils
├─ validation
└─ web
   ├─ controller
   ├─ doc
   ├─ dto
   │  ├─ groups
   │  ├─ mapper
   │  │  └─ annotations
   │  └─ serializer
   └─ exception
```

Rules:

- Features are grouped inside the same package structure.
- DTOs are located in `/web/dto`.
- MapStruct mappers are located in `/web/dto/mapper`.
- Entities are located in `/entity`.

## Directory Structure

Corrected project structure following modular monolithic architecture:

```
br/com/threadstech/stockfy
├─ api
├─ components
├─ config
│  ├─ constraints
│  └─ properties
├─ core
│  ├─ enums
│  ├─ exception
│  ├─ utils
│  └─ validation
├─ modules
│  └─ example
│     ├─ ExampleController.java
│     ├─ ExampleService.java
│     ├─ ExampleRepository.java
│     ├─ Example.java
│     ├─ ExampleControllerDoc.java
│     ├─ ExampleMapper.java
│     ├─ usecase       # only if there are multiple use cases
│     │  ├─ CreateExampleUseCase.java
│     │  └─ UpdateExampleUseCase.java
│     └─ dto           # only if there are multiple Dtos
│        ├─ mapper
│        │  └─ ExampleMapper.java
│        ├─ ExampleDto.java
│        └─ ExampleSummaryDto.java
├─ security
│  ├─ jwt
│  └─ refreshtoken
└─ web
   └─ exception
```

Rules:

- Features are **isolated in their own modules** within `/modules`.
- Controller, Service, Repository, Entity, and ControllerDoc are **single files per module**.
- Shared code (utils, enums, validations, exceptions) is placed in `/core`.
- `web/exception` is for global exceptions only.
- Module-specific exceptions must be placed within the module’s `exception` package. Example:
  `product.exception.ProductInvalidException`

## Controllers

- Controllers return **DTOs**.
- Exception: simple responses may return `String`.
- There is **no global response wrapper pattern**.
- Response structure depends on each DTO.

Validation and errors:

- Validation using `@Valid`.
- Global exception handling via `ControllerAdvice`.
- Unique constraint handling via `ConstraintResolver` located in `/config/constraints`.

## Services

- Services contain business logic and application orchestration.
- [LEGACY] There is **no separation between service and use case layers**.
- There is a **separation between services and use cases**.
- Services directly call repositories.
- Services **do not use interfaces**.

## Persistence

- ORM: Spring Data JPA
- Entities map directly to database tables.
- There is **no separation between entity and domain model** [LEGACY].
- There is a **separation between the entity and domain models**.

Query rules:

- Complex queries use **native SQL**.
- Native SQL must remain **PostgreSQL compatible**.

## DTOs and Mapping

- DTOs are used for both request and response.
- Mapping is implemented with **MapStruct**.
- [LEGACY] Mappers are located in `/web/dto/mapper`.
- Mappers are located within the module’s `dto.mapper` package. Example: `product.dto.mapper.ProductMapper`

## Transactions

- Transactions are defined with `@Transactional`.
- `@Transactional` must be placed **inside services**.
- Explicit transaction management is **not used**.

## Security

- Framework: Spring Security
- Authentication: JWT stored in **HTTP-only cookies**
- Authorization: **Role-based**

Controllers:

- `AuthController` handles authentication
- `UserController` / `EmployeeController` handle user management

### Rules

- Security tests must be created **only after the feature is fully implemented**
- All **unit and integration tests must pass before creating security tests**
- Security tests must be treated as **integration tests**, not unit tests
- Security tests must validate the system with the **real security configuration loaded (ApplicationContext)**

### Rate Limiting

Rate limiting will be applied to protect endpoints against abuse, brute-force attacks, and system overload.

#### Strategy

- Rate limiting by IP for public endpoints (using a hashed IP)
- Rate limiting by authenticated user for protected endpoints
- Different limits per endpoint type (e.g., stricter limits for authentication)
- HTTP 429 (Too Many Requests) responses when the limit is exceeded

#### Client Identification

- The IP address **will not be used in its raw form**
- A **hash (e.g., SHA-256)** will be applied before using it as the rate limit key
- The hash will be the key used by Bucket4j to manage buckets
- Objective: reduce exposure of personal data and mitigate risks in case of data leaks

#### Dependency

The following library will be used:

- `bucket4j-core`

#### Justification

- Token bucket–based implementation (efficient and predictable)
- Compatible with Spring Boot
- Allows fine-grained control of limits (by key: hashed IP, user, endpoint)
- Supports distributed storage (e.g., Redis) for future scalability

#### Possible Stack

- `bucket4j-core`
- `bucket4j-redis` (if horizontal scaling becomes necessary)

#### Notes

- Initial configuration will be in-memory
- It can be migrated to Redis later without impacting business logic
- Rate limiting logic should be applied via a filter or interceptor in Spring
- Bucket4j does not perform hashing automatically; the key must be defined manually by the application

## Configuration

- Configuration classes are located in `/config`.
- Infrastructure and application configuration are **not separated**.

Profiles:

- `dev`
- `prod`
- `test`

## Testing

Testing strategy:

- Unit tests
- Integration tests
- TestContainers

Naming conventions:

- Unit tests: `<Resource><Scope>Test`
- Integration tests: `<Resource>IT`

Example:

- `EmployeeServiceTest`
- `EmployeeControllerTest`
- `EmployeeRepositoryTest`
- `EmployeeIT`

Test method naming:

```
should<Action>Return<Status>
```

Example:

```
shouldCreateProductReturnCreated
```

## Additional Patterns

- Standard Spring project structure [LEGACY]
- No event-driven architecture [LEGACY]
- Modular Architecture
- Event-driven architecture

Background processes:

- Schedulers exist in `/service/scheduler` [LEGACY]
- Bootstrapping logic exists for initial admin user creation
- Schedulers update system metrics [LEGACY]

## Project Conventions

### Resource Naming

Resources follow the pattern:

```
<Resource><Scope>
```

Examples:

- `EmployeeController`
- `EmployeeService`
- `EmployeeRepository`
- `EmployeeDto`

### Service Method Naming

Service methods follow:

```
<action><resource>
```

Examples:

- `findProduct`
- `createProduct`
- `updateProduct`
- `deleteProduct`

### Endpoint Naming

Rules:

- Parent endpoints are defined in `/api/ApiPaths`.
- Parent endpoints are always **plural**.
- Child endpoints must also be **plural**.

Rules for common endpoints:

- Primary endpoints (e.g. `findAll`, `create`) **do not require child paths**.

Examples:

```
GET /products
POST /products
```

Additional endpoints must use child paths:

```
GET /products/{id}
```

If multiple endpoints share the same structure (e.g. `/{id}` and `/{name}`), they must be disambiguated:

```
/products/{id}/id
/products/{name}/name
```

### Mapper Method Naming

Mapper methods follow:

```
to<Resource>
to<Resource><Type>
```

Examples:

- `toProduct`
- `toProductList`
- `toProductPage`

## Database

- Database: PostgreSQL

Current state:

- Schema generation handled by Spring
- Migrations are **not currently used**

Future plan:

- Flyway migrations will be introduced

Rule:

- Agents must **only introduce Flyway if explicitly requested**.

## Observability

### Logging

- Application logs are stored in a Hibernate Envers tables.

### Metrics

- Metrics are updated by **scheduled jobs**.
- Metrics track data such as:

    - Sales
    - Customers

### Alerts

- Alerts are **temporarily persisted**.
- Alerts are cleared when the user performs an action that resolves the problem.

### Audit

- Audit implemented using `AuditorAware` and `Hibernate Envers`.

## Restrictions (What the Agent Must NOT Do)

- The agent must NOT modify or remove any content inside the .agent directory (CRITICAL).
- The agent must NOT create methods whose sole responsibility is to construct DTOs. In these cases, prefer using the
  DTO's own builder or constructor.
- The agent must NOT execute Maven tests without wrapping the `-Dtest` flag in quotes.

    - Incorrect: `mvn test -Dtest=DashboardIT,DashboardServiceTest`
    - Correct: `mvn test "-Dtest=DashboardIT,DashboardServiceTest"`

- The agent must NOT add profile-specific fields to the main `application.yaml`. Fields defined in
  `application-prod.yaml` and/or `application-dev.yaml` must remain isolated to those profiles and must not be
  duplicated in `application.yaml`.
- Do not add comments to the code unless explicitly requested by the user.
- NEVER use hardcoded values for environment variables, API keys, secrets, etc. (CRITICAL)