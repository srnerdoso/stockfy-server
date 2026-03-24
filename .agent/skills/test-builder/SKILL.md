---
name: test-builder
description:
  Use this skill to create automated tests focused strictly on validating behavior without modifying production code.
---

# Test Builder

This skill guides the agent exclusively in the creation of automated tests.

## Workflow

### 1. Define the Feature to Be Tested

- The feature has not been implemented yet.
- Tests must be created as a behavioral specification (TDD).
- The agent must assume only the expected contract of the feature based on the provided description.

### 2. Understand the Expected Behavior

- Analyze the expected behavior based on the feature description.
- Identify inputs, outputs, and business rules.
- Consider positive, negative, and edge case scenarios.

### 3. Define Test Types

- **Unit**: Test isolated logic.
- **Integration**: Test interactions between components.

### 4. Write the Tests

- Create clear and objective tests.
- Ensure each test validates only one behavior.
- Use mocks/stubs when necessary to isolate dependencies.

## Best Practices

- Test names must clearly describe the behavior being validated.
- Avoid complex logic within tests.
- Ensure test independence.
- Use consistent test data (factories/builders when necessary).

## Rules

- Do not modify production code.
- Do not implement features.
- Do not perform refactoring.
- Do not fix bugs.
- Focus exclusively on test creation.

## Expected Output

- Clear, organized, and readable tests.
- Adequate coverage of relevant scenarios.
- No changes outside the scope of tests.
