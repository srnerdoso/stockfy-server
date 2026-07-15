# Testing Guidelines

## Overview

This document defines all rules, standards, and conventions related to test creation within the project.

---

## Core Principle

Tests must be **grouped by module and type**, not scattered across multiple files.

* Do not create new test classes unnecessarily
* Always prefer extending existing test classes

---

## Reuse Rules

When implementing new features:

* The agent must **not create new test classes** unless explicitly requested
* The agent must search for existing test classes within the same module/resource

### If a test class exists

* Add new tests to the existing class

### If a test class does not exist

* The agent is allowed to create a new test class
* The class must follow the project's naming and structure conventions

---

## Test Type Mapping

Tests must be added according to their type:

* **Integration tests** → existing integration test class of the module
* **Unit tests** → existing unit test class
* **E2E tests** → existing E2E test class

Each test type must remain in its respective class.

---

## Module-Based Organization

Tests must be organized per module/resource.

### Example

For a `product` module:

* Integration tests → `ProductIntegrationTest`
* Unit tests → `ProductServiceTest`
* E2E tests → `ProductE2ETest`

The agent must locate and reuse these classes when adding new tests.

---

## Duplication Rules

The agent must avoid:

* Creating multiple test classes for the same module and type
* Duplicating setup or configuration across test classes
* Fragmenting tests across unrelated files

---

## Anti-Patterns (Forbidden)

The agent must never:

* Create new test classes when a suitable one already exists
* Duplicate test structure across multiple files
* Mix different test types in the same class
* Ignore existing test organization

---

## Goal

Ensure:

* Centralized and maintainable test structure
* Reduced duplication
* Clear separation by test type
* Consistent organization across the codebase
