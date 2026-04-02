---
name: code-review-process
description:
  Use this skill to review code while considering the project’s architecture, conventions, naming standards, and
  patterns already defined in the global project context.
---

# Code Review Process 

This skill guides the agent to perform consistent code reviews, using the rules defined in the global context as the
single source of truth.

## Objective

Ensure that the analyzed code:

- Follows the project’s architecture
- Respects the defined conventions and standards
- Uses clear and consistent naming
- Maintains readability and maintainability

## Principles

- Never invent rules: use only what is already present in the global context
- Prioritize consistency with the rest of the project
- Be objective: point out the problem + suggest a correction
- Avoid generic comments

## Workflow

### 1. Identify Context

- Read the provided code
- Identify:
    - Programming language
    - Layer (controller, service, etc.)
    - Responsibility of the code snippet

### 2. Validate Architecture

Check whether the code:

- Is in the correct layer
- Respects separation of concerns
- Avoids unnecessary coupling

If there is an issue:

- Explain the error
- Suggest the proper reorganization

### 3. Validate Conventions

Compare against the project’s defined standards (global context):

- File structure
- Import organization
- Formatting

Only point out actual deviations.

### 4. Validate Naming

Analyze names of:

- Variables
- Functions
- Classes

Criteria:

- Clarity
- Explicit intent
- Consistency with the project

If needed:

- Suggest better names

### 5. Validate Code Quality

Check for:

- Overly large functions
- Duplicated code
- Unnecessary complexity
- Lack of proper abstraction

Suggest direct improvements.

### 6. Structured Output

Always respond in the following format:

- **Problem:**  
  Direct description

- **Impact:**  
  Why this is problematic

- **Suggestion:**  
  How to fix it (objective and actionable)

### 7. Pull Request Generation (READ-ONLY)

After completing the review, generate a professional Pull Request description
based on the analyzed code.

The PR must follow these rules:

#### Scope Validation

- Ensure the changes represent a single concern:
  - feature OR
  - fix OR
  - refactor

If multiple concerns are detected:
- explicitly recommend splitting into multiple PRs

#### Title Generation

- Write a clear and technical title
- Reflect the intent of the change
- Avoid generic titles like:
  - "update"
  - "changes"

#### Description Generation

Generate a structured PR description using:

- Context: Why this change exists
- Changes: What was implemented
- How to Test: Steps to validate the behavior
- Risks: Possible side effects or impacts

#### Additional Context (if applicable)

- Mention related issues (e.g., "Fixes #ID")
- Highlight critical or complex parts of the code
- Suggest where the reviewer should start

#### Constraints (MANDATORY)

- DO NOT modify code
- DO NOT generate patches
- DO NOT rewrite implementations

This step is strictly descriptive and analytical.

If issues were found:
- reference them
- do NOT fix them
- assume another agent will handle corrections

---

## Important Rules

- Do not assume any context that has not been provided
- Do not praise the code
- Do not explain basic concepts
- Focus only on relevant issues

## Example Output

- **Problem:**  
  Variable name `date` is too generic

- **Impact:**  
  Makes it difficult to understand the variable’s purpose

- **Suggestion:**  
  Rename it to `userCreatedAt` or a similarly descriptive name