---
name: feature-builder
description:
  Use this skill to implement new features while preserving the integrity of the existing codebase and enforcing test-first development.
---

# Feature Builder

This skill guides the agent in implementing new features safely and predictably.
The agent must prioritize test-first development and must never modify existing
code or project dependencies without explicit user permission.

## Core Rules

* The agent **must not edit or remove existing files or code**.
* The agent **must not introduce new dependencies** unless explicitly requested by the user.
* If an action requires modifying existing code or adding dependencies, the agent **must ask for permission first**.
* The agent may only **add new files required for the feature**.

## Workflow

### 1. Create Integration Tests First

Before implementing any feature logic, the agent must create integration tests
for the feature.

Integration tests must validate:

* HTTP response status
* Full response body structure
* Explicit verification of **all response fields**
* Database persistence

Tests must ensure that:

* The API returns the correct status code
* Every response field is explicitly asserted
* The expected data is persisted in the database

### 2. Define Expected Behavior

From the integration test, define:

* Endpoint behavior
* Expected request format
* Expected response structure
* Persistence expectations

The test should clearly describe the feature behavior.

### 3. Implement the Feature

After tests are created, implement the minimal code required for the tests to pass.

Implementation must follow the existing project architecture and conventions.

Constraints:

* Do not refactor existing code
* Do not remove or modify existing files
* Do not introduce dependencies

### 4. Validate Behavior

Run the integration tests and confirm:

* Tests pass successfully
* The response matches the expected structure
* Data is correctly persisted

### 5. Ask Before Risky Operations

If the feature requires any of the following, the agent must ask the user first:

* Editing existing files
* Refactoring existing logic
* Adding new dependencies

The agent must never perform these actions automatically.

## 6. Internationalization and Messages

The agent **must not use hardcoded string values** in API responses, including but not limited to:

* Error messages
* Validation messages
* Success messages
* Any user-facing response text

All response messages must be retrieved using **Spring's MessageSource**.

Rules:

* Always resolve messages through `MessageSource`
* Message keys must be defined in the project's message resource files
* The agent must reference the message key instead of embedding literal strings in the code

This ensures that all responses support proper **internationalization (i18n)** and maintain consistency across the application.

## 7. OpenAPI Documentation

All endpoints must be documented using **Swagger OpenAPI** through dedicated documentation interfaces.

Rules:

* The agent must create a **public interface** inside the directory `/web/doc`.
* This interface is responsible **only for endpoint documentation**.
* Controllers must **extend the corresponding documentation interface**.

Naming convention:

* Documentation interfaces must follow the pattern: `<Resource>ControllerDoc`
* Examples:
    * `AuthControllerDoc`
    * `UserControllerDoc`

Responsibilities:

* The documentation interface must contain all **OpenAPI annotations** (`@Operation`, `@ApiResponse`, `@Parameter`, etc.).
* Controllers must only implement the logic and extend the documentation interface.

This approach keeps **documentation separated from implementation**, improving readability and maintainability.