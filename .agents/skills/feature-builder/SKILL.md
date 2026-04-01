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
* Message source integration: Verify that error messages are correctly resolved from the message source for the current
  locale, including parameterized messages and fallback behavior for missing keys
* Security tests: Validate access with **authorized roles** and **unauthorized roles**, ensuring proper permission
  enforcement and expected HTTP responses (e.g., 200, 403, 401)

Tests must ensure that:

* The API returns the correct status code
* Every response field is explicitly asserted
* The expected data is persisted in the database

All tests must strictly follow `references/testing-guidelines.md`.

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

## Compliance and Standards

All implementations must comply with the project's technical standards and guidelines.

### Standards

The agent must strictly follow:

- `references/internationalization.md`: Defines rules for message externalization using MessageSource. Prohibits hardcoded strings and enforces i18n across all API responses.

- `references/openapi-guidelines.md`: Defines how endpoints must be documented using OpenAPI via dedicated documentation interfaces, enforcing separation between documentation and implementation.

- `references/rate-limiting.md`: Defines the rate limiting strategy using Bucket4j, including client identification, infrastructure integration, and expected behavior (HTTP 429).

These standards must be applied before and during implementation.

The agent must not implement or expose any endpoint that violates these rules.

Any modification to existing infrastructure (e.g., filters, interceptors, or rate limiting configuration) requires explicit user permission.