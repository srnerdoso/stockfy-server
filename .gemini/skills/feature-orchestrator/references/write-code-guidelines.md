# Write Code Guidelines

This document defines the mandatory standards for writing code and implementing new features in the project.

## 1. Internationalization (i18n)

The core principle is **complete separation between source code and text**.

- **No Hardcoding:** Literal strings for user-facing messages (errors, validations, success messages) are strictly prohibited in controllers or services.
- **Message Resolution:** Use `MessageSource` from Spring exclusively to resolve messages.
- **Parameterized Messages:** Use placeholders (e.g., `{0}`) in property files and pass arguments via `MessageSource`. Never concatenate strings to build messages.
- **Message Keys:** Keys must be descriptive, consistent, and grouped by domain (e.g., `user.not.found`).
- **Scope:** Only internal logs and developer debug messages can bypass internationalization.

## 2. OpenAPI Documentation (Swagger)

Documentation must be **decoupled from business logic** to maintain clean code.

- **Documentation Interfaces:** Each resource should have a dedicated interface following the pattern `<Resource>ControllerDoc`.
- **Annotation Location:** All Swagger annotations (`@Operation`, `@ApiResponse`, `@Schema`, etc.) must reside exclusively in the interface.
- **Controller Implementation:** Controllers should extend the corresponding documentation interface and contain only the endpoint logic, free of documentation annotations.

## 3. Rate Limiting and Security

Traffic control must be centralized and never implemented in the application layer (business logic).

- **Technology:** Use **Bucket4j** integrated with the existing security flow.
- **Client Identification:**

    - **Public Endpoints:** Use the hash of the IP address (SHA-256). Never use raw IP.
    - **Protected Endpoints:** Use the authenticated user's identifier via the security context.
- **Logic Location:** Rate limiting must be applied via **filters or interceptors** in the infrastructure. Implementation in Controllers or Services is prohibited.

## 4. Architecture and Integrity

Rules for project modification:

- **Code Preservation:** Do not edit or remove existing files or code without explicit permission.
- **New Features:** Limit yourself to adding new files required for the feature.
- **Dependencies:** Do not introduce new dependencies in `pom.xml` or `build.gradle` without prior authorization.
- **High-Risk Operations:** Refactoring existing logic or changing infrastructure filters requires user approval.

## 5. Prohibited Anti-Patterns

* Concatenating strings to build error messages.
* Returning raw exception messages directly to the client.
* Placing OpenAPI annotations directly in `@RestController` classes.
* Implementing custom rate limiting logic outside the project's Bucket4j standard.
* Ignoring `MessageSource` for defining the body of HTTP 429 responses.
