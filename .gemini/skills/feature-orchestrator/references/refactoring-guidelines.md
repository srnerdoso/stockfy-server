# Refactoring Guide

This document serves as a reference for the standard procedures and rules for code refactoring, ensuring project
integrity and consistency.

---

## Refactoring Categories

Select the appropriate workflow based on the type of change required:

* **Logic Refactoring**: Changes affecting behavior, rules, or data flow.
* **Readability Refactoring**: Improvements to clarity without altering behavior.
* **Controller Refactoring**: Changes involving controller classes and their contracts.

---

## Technical Workflows

### Logic and Controller Refactoring

When changes impact logic, behavior, business rules, or controller contracts, follow these steps:

1. **Update Tests First**: Modify or create tests to define the new expected behavior before implementation.
2. **Freeze Test Changes**: Once updated, tests become the single source of truth and must not be modified further.
3. **Refactor Implementation**: Update the code to satisfy the tests while maintaining architectural patterns.

### Readability Refactoring

For improvements strictly focused on code clarity:

* **Requirements**: Do not modify tests, logic, behavior, or execution flow.
* **Permitted Actions**: Rename variables/methods/classes, simplify expressions, improve formatting, and extract methods
  for clarity.
* **Prohibitions**: Do not change business logic, modify control flow, add features, or add documentation/comments
  unless requested.

---

## General Guidelines

To maintain code quality during any refactoring process:

* **Scope**: Keep changes minimal, focused, and within the requested scope.
* **Integrity**: Preserve the existing architecture, design patterns, and codebase style.
* **Documentation**: Changes to documentation classes are only permitted during controller refactoring if required.