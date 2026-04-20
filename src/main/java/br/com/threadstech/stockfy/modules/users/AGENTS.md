# Module: Users

The `Users` module is the central authority for Identity and Access Management (IAM) in Stockfy. It handles authentication, authorization, auditing, and security-related rate limiting.

## Key Technical Decisions

- **Authentication**: JWT-based with tokens stored in **HttpOnly Cookies**. **Redis** is used to manage Refresh Tokens and session invalidation.
- **Authorization**: **RBAC (Role-Based Access Control)** with roles (`ADMIN`, `USER`) and granular permissions.
- **Auditing**: Full entity auditing using **Hibernate Envers** and `AuditorAware` for transparency.
- **Rate Limiting**: **Bucket4j** implementation.
  - Login/Auth: 5-15 req/min.
  - General API: 10 req/min.
- **Soft Delete**: Entities use an `active` boolean field; permanent deletion is prohibited for audit integrity.
- **EDA**: Security alerts (e.g., multiple failed logins, unauthorized access attempts) are published via **RabbitMQ**.

## Architectural Standards

- **Hexagonal Architecture**: Strictly separate Domain, Application, and Infrastructure layers.
- **DDD**: Centralize all security invariants within the Domain layer.
- **TDD**: Write tests for all security rules and rate-limiting logic before implementation.
- **Security**: Never expose sensitive data (passwords, internal IDs) in DTOs. Use MapStruct for safe mapping.

## Guidelines for AI Agents

- Ensure every new entity in this module implements the `active` field for soft delete.
- All service-level mutations must be audited.
- Cross-module communication must happen via interfaces/events, never by direct repository access.
- Validate rate limits in integration tests.
