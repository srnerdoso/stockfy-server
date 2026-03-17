   ---
name: refactoring-agent
description:
Use this skill to refactor code safely and consistently. Supports both logic
changes and readability improvements while preserving project integrity.
---

# Refactoring Agent

This skill guides the agent in performing controlled and predictable code refactoring.

## Workflow

### 1. Determine Refactoring Type

- **Logic Refactoring**: Changes that affect behavior, rules, or data flow.
- **Readability Refactoring**: Changes that improve clarity without altering behavior.
- **Controller Refactoring**: Changes related to controller classes and their contracts.

---

## Logic Refactoring Rules

When the refactoring impacts logic, behavior, or business rules:

### Step 1: Update Tests First

- Modify or create tests to reflect the new expected behavior.
- Ensure tests clearly define the intended logic after refactoring.
- Do not proceed to implementation until tests are updated.

### Step 2: Freeze Test Changes

- After updating tests, DO NOT modify them anymore.
- Tests become the single source of truth for the new behavior.
- Only change tests again if explicitly requested by the user.

### Step 3: Refactor Implementation

- Update the code to satisfy the new tests.
- Ensure all tests pass after refactoring.
- Maintain consistency with project architecture and patterns.

---

## Controller Refactoring Rules

When the refactoring is related to controllers:

- Follow the same rules as **Logic Refactoring** or **Readability Refactoring**, depending on the type of change.
- Changes to documentation classes are allowed if required.

## Readability Refactoring Rules

When the refactoring is strictly about improving code readability:

- DO NOT modify any tests.
- DO NOT change logic, behavior, or execution flow.
- Only improve structure and clarity.

### Allowed Changes

- Rename variables, methods, and classes for clarity.
- Simplify conditionals and expressions.
- Improve formatting and structure.
- Extract small methods if it improves readability.

### Forbidden Changes

- Changing business logic.
- Modifying control flow.
- Adding new features or behaviors.
- Adding comments or documentation unless explicitly requested.

---

## General Guidelines

- Keep changes minimal and focused.
- Avoid unnecessary modifications outside the requested scope.
- Preserve existing architecture and design patterns.
- Ensure consistency with the codebase style.