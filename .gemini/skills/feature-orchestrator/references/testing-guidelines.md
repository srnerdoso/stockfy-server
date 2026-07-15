# Testing Guidelines

## Overview

This document defines the rules, standards, and workflow for the **feature-orchestrator** skill, focusing strictly on
validating behavior through centralized and maintainable automated tests. The goal is to ensure a clear separation by
test type and consistent organization across the codebase without modifying production code.

---

## Core Principles & Rules

- **No Production Changes**: Do not modify production code, implement features, perform refactoring, or fix bugs.
- **Strict Test Focus**: Focus exclusively on test creation as a behavioral specification (TDD).
- **Centralized Structure**: Tests must be **grouped by module and type**, not scattered across multiple files.
- **Reuse First**: Do not create new test classes unnecessarily; always prefer extending existing ones.
- **Independence**: Ensure each test validates only one behavior and remains independent from others.

---

## Workflow

### 1. Feature Analysis

- **Define the Feature**: Assume only the expected contract based on the provided description for features not yet
  implemented.
- **Understand Behavior**: Identify inputs, outputs, business rules, and consider positive, negative, and edge case
  scenarios.

### 2. Test Type Mapping

Tests must be categorized and added to their respective existing classes within the module:

- **Unit Tests**: Focused on isolated logic (e.g., `ProductServiceTest`).
- **Integration Tests**: Focused on interactions between components (e.g., `ProductIntegrationTest`).
- **E2E Tests**: Focused on end-to-end flows (e.g., `ProductE2ETest`).

### 3. Execution & Implementation

- **Search and Locate**: Search for existing test classes within the same module/resource before creating anything new.
- **Class Creation**: Only create a new test class if one does not exist for that specific module and type.
- **Validation**: Use mocks or stubs when necessary to isolate dependencies and ensure clear, objective validation.

---

## Organization & Reuse Standards

### Module-Based Structure

The project follows a strict module/resource organization.
> **Example for a `product` module:**
> - Integration → `ProductIntegrationTest`
> - Unit → `ProductServiceTest`
> - E2E → `ProductE2ETest`

### Duplication Rules

- Avoid creating multiple test classes for the same module and type.
- Do not duplicate setup or configuration across different test classes.
- Do not fragment tests across unrelated files.

---

## Best Practices

- **Naming**: Test names must clearly describe the behavior being validated.
- **Simplicity**: Avoid complex logic within tests.
- **Data Consistency**: Use consistent test data, employing factories or builders when necessary.
- **Fidelity**: Implement tests faithfully according to the real implementation once it exists.

---

## Anti-Patterns (Forbidden)

* Creating new test classes when a suitable one already exists.
* Duplicating test structure across multiple files.
* Mixing different test types (e.g., Unit and Integration) in the same class.
* Ignoring the established test organization of the project.