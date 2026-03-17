# Project: Stockfy

## Architecture

- Layered Architecture
- Single module application
- Package-by-layer
- Controllers in `/web/controller`
- Services in `/service`
- Repositories in `/repository`
- Entities in `/entity`
- Packages can access services from other features (e.g. purchase features may access user services)

There is **no separation*- between domain, application, and infrastructure layers.

## Directory Structure

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
- There is **no separation between service and use case layers**.
- Services directly call repositories.
- Services **do not use interfaces**.

## Persistence

- ORM: Spring Data JPA
- Entities map directly to database tables.
- There is **no separation between entity and domain model**.

Query rules:

- Complex queries use **native SQL**.
- Native SQL must remain **PostgreSQL compatible**.

## DTOs and Mapping

- DTOs are used for both request and response.
- Mapping is implemented with **MapStruct**.
- Mappers are located in `/web/dto/mapper`.

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

- Standard Spring project structure
- No event-driven architecture

Background processes:

- Schedulers exist in `/service/scheduler`
- Bootstrapping logic exists for initial admin user creation
- Schedulers update system metrics

Future features:

- Metrics system
- Audit logging system

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

- Application logs are stored in a **database table**.

### Metrics

- Metrics are updated by **scheduled jobs**.
- Metrics track data such as:

    - Sales
    - Customers

### Alerts

- Alerts are **temporarily persisted**.
- Alerts are cleared when the user performs an action that resolves the problem.

### Audit

- Audit implemented using `AuditorAware`.
- Audit logs are stored in a **database table**.

Current implementation status:

- `AuditorAware` implemented
- Full audit logging still pending

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